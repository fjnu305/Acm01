from __future__ import annotations

from typing import Literal

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field

from app.services import credentials as cred_store
from app.services import llm as llm_client
from app.services import vault

router = APIRouter()


class CredentialUpdate(BaseModel):
    provider: str = "openai_compatible"
    baseUrl: str | None = None
    model: str | None = None
    apiKey: str | None = None


class AskRequest(BaseModel):
    question: str = Field(min_length=1, max_length=4000)
    mode: Literal["quick", "standard", "deep"] = "standard"


@router.get("/health")
def health():
    try:
        status = vault.vault_status()
        return {"ok": True, **status}
    except FileNotFoundError as e:
        return {"ok": False, "error": str(e)}


@router.get("/vault/status")
def vault_status():
    try:
        return vault.vault_status()
    except FileNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e)) from e


@router.get("/vault/page")
def vault_page(path: str):
    try:
        return vault.read_page(path)
    except FileNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e)) from e


@router.get("/graph")
def graph():
    try:
        return vault.build_graph()
    except FileNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e)) from e


@router.get("/credentials")
def get_credentials():
    return cred_store.public_view()


@router.put("/credentials")
def put_credentials(body: CredentialUpdate):
    try:
        return cred_store.save_credentials(
            provider=body.provider,
            base_url=body.baseUrl or "",
            model=body.model or "",
            api_key=body.apiKey,
        )
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e)) from e


@router.delete("/credentials")
def delete_credentials():
    cred_store.delete_credentials()
    return {"ok": True}


class CredentialTest(BaseModel):
    baseUrl: str | None = None
    model: str | None = None
    apiKey: str | None = None


@router.post("/credentials/test")
async def test_credentials(body: CredentialTest):
    try:
        stored = cred_store.load_raw()
        api_key = (body.apiKey or "").strip() or None
        base_url = (body.baseUrl or "").strip().rstrip("/") or None
        model = (body.model or "").strip() or None

        if not api_key:
            if not stored:
                raise HTTPException(status_code=400, detail="请先填写 API Key 或保存凭证后再测试")
            base_s, model_s, key_s = cred_store.resolve_for_call()
            api_key = key_s
            base_url = base_url or base_s
            model = model or model_s
        else:
            if not base_url or not model:
                raise HTTPException(status_code=400, detail="测试时请同时提供 Base URL 与模型名")

        reply = await llm_client.test_connection(
            base_url=base_url,
            model=model,
            api_key=api_key,
        )
        return {"ok": True, "reply": (reply or "")[:80]}
    except HTTPException:
        raise
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e)) from e
    except RuntimeError as e:
        raise HTTPException(status_code=502, detail=str(e)) from e


@router.post("/ask")
async def ask(body: AskRequest):
    question = body.question.strip()
    if not question:
        raise HTTPException(status_code=400, detail="question is required")

    try:
        context_pages = vault.load_context_for_ask(body.mode, question)
    except FileNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e)) from e

    if not cred_store.load_raw():
        raise HTTPException(
            status_code=400,
            detail="请先在设置中配置 DeepSeek / OpenAI 兼容 API Key",
        )

    context_blocks = []
    for i, page in enumerate(context_pages, 1):
        context_blocks.append(
            f"[{i}] {page['title']} ({page['path']})\n{page['excerpt']}"
        )
    context_text = "\n\n---\n\n".join(context_blocks) if context_blocks else "(无检索到笔记)"

    system = (
        "你是 ACMer 学习笔记助手。只根据提供的 wiki 笔记回答。"
        "若笔记不足以回答，请明确说明不确定，不要编造。"
        "在相关处用 [n] 标注引用来源编号。"
        "用中文回答，简洁有条理。"
    )
    user = f"用户问题：\n{question}\n\n可用笔记：\n{context_text}"

    try:
        answer = await llm_client.chat_completion(system=system, user=user)
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e)) from e
    except RuntimeError as e:
        raise HTTPException(status_code=502, detail=str(e)) from e

    return {
        "answer": answer,
        "mode": body.mode,
        "citations": [
            {"index": i, "path": p["path"], "title": p["title"]}
            for i, p in enumerate(context_pages, 1)
        ],
    }

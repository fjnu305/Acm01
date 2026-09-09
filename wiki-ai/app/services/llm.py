from __future__ import annotations

import httpx

from app.services import credentials as cred_store


async def chat_completion(
    *,
    system: str,
    user: str,
    temperature: float = 0.3,
    max_tokens: int | None = None,
    base_url: str | None = None,
    model: str | None = None,
    api_key: str | None = None,
) -> str:
    if base_url and model and api_key:
        resolved_base, resolved_model, resolved_key = base_url.rstrip("/"), model, api_key
    else:
        resolved_base, resolved_model, resolved_key = cred_store.resolve_for_call()

    url = f"{resolved_base}/chat/completions"
    headers = {
        "Authorization": f"Bearer {resolved_key}",
        "Content-Type": "application/json",
    }
    payload: dict = {
        "model": resolved_model,
        "temperature": temperature,
        "messages": [
            {"role": "system", "content": system},
            {"role": "user", "content": user},
        ],
    }
    if max_tokens is not None:
        payload["max_tokens"] = max_tokens

    async with httpx.AsyncClient(timeout=90.0) as client:
        resp = await client.post(url, headers=headers, json=payload)
        if resp.status_code >= 400:
            detail = resp.text[:500]
            raise RuntimeError(f"LLM HTTP {resp.status_code}: {detail}")
        data = resp.json()
        try:
            return data["choices"][0]["message"]["content"]
        except (KeyError, IndexError, TypeError) as e:
            raise RuntimeError(f"Unexpected LLM response: {data}") from e


async def test_connection(
    *,
    base_url: str | None = None,
    model: str | None = None,
    api_key: str | None = None,
) -> str:
    return await chat_completion(
        system="Reply with exactly: ok",
        user="ping",
        temperature=0,
        max_tokens=8,
        base_url=base_url,
        model=model,
        api_key=api_key,
    )

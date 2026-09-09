from __future__ import annotations

import json
from pathlib import Path
from typing import Any

from app.config import settings
from app.crypto import decrypt, encrypt, mask_key


def _store_path() -> Path:
    return settings.resolved_data_dir() / "llm_credentials.json"


def load_raw() -> dict[str, Any] | None:
    path = _store_path()
    if not path.exists():
        return None
    return json.loads(path.read_text(encoding="utf-8"))


def save_credentials(
    *,
    provider: str,
    base_url: str,
    model: str,
    api_key: str | None,
) -> dict[str, Any]:
    existing = load_raw() or {}
    encrypted_key = existing.get("encrypted_api_key")
    key_iv = existing.get("key_iv")

    if api_key is not None and api_key.strip():
        encrypted_key, key_iv = encrypt(api_key.strip())
    elif not encrypted_key:
        raise ValueError("apiKey is required for the first save")

    payload = {
        "provider": provider or "openai_compatible",
        "base_url": (base_url or settings.default_base_url).rstrip("/"),
        "model": model or settings.default_model,
        "encrypted_api_key": encrypted_key,
        "key_iv": key_iv,
    }
    _store_path().write_text(json.dumps(payload, ensure_ascii=False, indent=2), encoding="utf-8")
    return public_view(payload)


def public_view(raw: dict[str, Any] | None = None) -> dict[str, Any]:
    data = raw if raw is not None else load_raw()
    if not data:
        return {
            "configured": False,
            "provider": None,
            "baseUrl": settings.default_base_url,
            "model": settings.default_model,
            "apiKeyMasked": None,
        }
    plain = None
    try:
        plain = decrypt(data["encrypted_api_key"], data["key_iv"])
    except Exception:
        plain = None
    return {
        "configured": True,
        "provider": data.get("provider", "openai_compatible"),
        "baseUrl": data.get("base_url", settings.default_base_url),
        "model": data.get("model", settings.default_model),
        "apiKeyMasked": mask_key(plain),
    }


def resolve_for_call() -> tuple[str, str, str]:
    """Returns (base_url, model, api_key)."""
    data = load_raw()
    if not data:
        raise ValueError("LLM credentials not configured")
    api_key = decrypt(data["encrypted_api_key"], data["key_iv"])
    base_url = data.get("base_url") or settings.default_base_url
    model = data.get("model") or settings.default_model
    return base_url.rstrip("/"), model, api_key


def delete_credentials() -> None:
    path = _store_path()
    if path.exists():
        path.unlink()

from __future__ import annotations

import base64
import os
import secrets

from cryptography.hazmat.primitives.ciphers.aead import AESGCM

from app.config import settings


def _load_or_create_key() -> bytes:
    raw = settings.credential_key_base64.strip()
    if raw:
        key = base64.b64decode(raw)
        if len(key) != 32:
            raise RuntimeError("CREDENTIAL_KEY_BASE64 must decode to 32 bytes")
        return key

    key_file = settings.resolved_data_dir() / "credential.key"
    if key_file.exists():
        return key_file.read_bytes()

    key = secrets.token_bytes(32)
    key_file.write_bytes(key)
    return key


_KEY = _load_or_create_key()
_AES = AESGCM(_KEY)


def encrypt(plaintext: str) -> tuple[str, str]:
    """Returns (ciphertext_b64, iv_b64)."""
    iv = secrets.token_bytes(12)
    ct = _AES.encrypt(iv, plaintext.encode("utf-8"), None)
    return base64.b64encode(ct).decode("ascii"), base64.b64encode(iv).decode("ascii")


def decrypt(ciphertext_b64: str, iv_b64: str) -> str:
    ct = base64.b64decode(ciphertext_b64)
    iv = base64.b64decode(iv_b64)
    return _AES.decrypt(iv, ct, None).decode("utf-8")


def mask_key(api_key: str | None) -> str | None:
    if not api_key:
        return None
    if len(api_key) <= 8:
        return "****"
    return f"{api_key[:4]}****{api_key[-4:]}"

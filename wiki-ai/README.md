"""
ACMer Wiki AI — local sidecar for LLM_Wiki + OpenAI-compatible BYOK.

Setup:
  cd wiki-ai
  python -m venv .venv
  .venv\\Scripts\\activate          # Windows
  pip install -r requirements.txt
  copy .env.example .env           # optional
  uvicorn app.main:app --host 127.0.0.1 --port 8787 --reload

API base: http://127.0.0.1:8787/wiki-api
"""

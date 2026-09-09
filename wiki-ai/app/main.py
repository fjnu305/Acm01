from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.routes.api import router

app = FastAPI(title="ACMer Wiki AI", version="0.1.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:5173",
        "http://127.0.0.1:5173",
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(router, prefix="/wiki-api")


@app.get("/")
def root():
    return {"service": "wiki-ai", "docs": "/docs", "api": "/wiki-api/health"}

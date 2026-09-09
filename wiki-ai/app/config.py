from pathlib import Path

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
    )

    vault_root: Path = Path("../LLM_Wiki")
    host: str = "127.0.0.1"
    port: int = 8787
    credential_key_base64: str = ""
    default_base_url: str = "https://api.deepseek.com/v1"
    default_model: str = "deepseek-chat"
    data_dir: Path = Path("data")

    def resolved_vault(self) -> Path:
        root = self.vault_root
        if not root.is_absolute():
            root = (Path(__file__).resolve().parent.parent / root).resolve()
        return root

    def resolved_data_dir(self) -> Path:
        d = self.data_dir
        if not d.is_absolute():
            d = (Path(__file__).resolve().parent.parent / d).resolve()
        d.mkdir(parents=True, exist_ok=True)
        return d


settings = Settings()

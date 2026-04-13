from pydantic_settings import BaseSettings
from functools import lru_cache


class Settings(BaseSettings):
    minimax_api_key: str = ""
    minimax_api_url: str = "https://api.minimaxi.com/v1"
    qdrant_host: str = "82.156.199.60"
    qdrant_port: int = 10007
    collection_name: str = "blog_knowledge"
    log_level: str = "INFO"
    embedding_model: str = "sentence-transformers/all-MiniLM-L6-v2"

    class Config:
        env_file = ".env"
        extra = "allow"


@lru_cache()
def get_settings() -> Settings:
    return Settings()

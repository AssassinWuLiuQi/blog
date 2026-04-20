from typing import List
from sentence_transformers import SentenceTransformer

from chat_service.config import get_settings

settings = get_settings()


class Embedder:
    """Text embedding using sentence-transformers."""

    def __init__(self):
        self.model = SentenceTransformer(settings.embedding_model)

    def embed(self, text: str) -> List[float]:
        """Embed a single text."""
        embedding = self.model.encode(text, convert_to_numpy=True)
        return embedding.tolist()

    def embed_batch(self, texts: List[str]) -> List[List[float]]:
        """Embed multiple texts."""
        embeddings = self.model.encode(texts, convert_to_numpy=True)
        return embeddings.tolist()

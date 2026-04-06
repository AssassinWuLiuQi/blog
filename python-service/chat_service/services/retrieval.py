from typing import List, Optional
from qdrant_client import QdrantClient
from qdrant_client.models import Distance, VectorParams, Filter
from sentence_transformers import SentenceTransformer
import numpy as np

from chat_service.config import get_settings

settings = get_settings()


class RetrievalService:
    """Vector retrieval service using Qdrant."""

    def __init__(self):
        self.client = QdrantClient(host=settings.qdrant_host, port=settings.qdrant_port)
        self.collection_name = settings.collection_name
        self.embedding_model = SentenceTransformer(settings.embedding_model)

    def ensure_collection(self, vector_size: int = 384):
        """Create collection if it doesn't exist."""
        collections = self.client.get_collections().collections
        collection_names = [c.name for c in collections]

        if self.collection_name not in collection_names:
            self.client.create_collection(
                collection_name=self.collection_name,
                vectors_config=VectorParams(size=vector_size, distance=Distance.COSINE)
            )

    def embed_texts(self, texts: List[str]) -> List[np.ndarray]:
        """Generate embeddings for texts."""
        embeddings = self.embedding_model.encode(texts, convert_to_numpy=True)
        return embeddings.tolist()

    def retrieve(self, query: str, top_k: int = 5) -> List[str]:
        """Retrieve relevant documents for a query."""
        query_embedding = self.embed_texts([query])[0]

        search_result = self.client.search(
            collection_name=self.collection_name,
            query_vector=query_embedding,
            limit=top_k,
            with_payload=True
        )

        return [hit.payload.get("content", "") for hit in search_result if hit.payload]

    def add_document(self, content: str, metadata: dict = None):
        """Add a document to the knowledge base."""
        embedding = self.embed_texts([content])[0]
        doc_id = str(hash(content))

        self.client.upsert(
            collection_name=self.collection_name,
            points=[
                {
                    "id": doc_id,
                    "vector": embedding,
                    "payload": {
                        "content": content,
                        **(metadata or {})
                    }
                }
            ]
        )

    def delete_document(self, doc_id: str):
        """Delete a document from the knowledge base."""
        self.client.delete(
            collection_name=self.collection_name,
            points_selector=Filter(
                must=[{"id": doc_id}]
            )
        )

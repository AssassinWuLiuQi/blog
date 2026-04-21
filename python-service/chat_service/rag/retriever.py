from typing import List, Dict
from qdrant_client import QdrantClient
from qdrant_client.models import PointStruct

from chat_service.rag.embedder import Embedder
from chat_service.config import get_settings

settings = get_settings()


class RAGRetriever:
    """RAG retriever: vector search in Qdrant."""

    def __init__(self):
        self.client = QdrantClient(
            url=f"http://{settings.qdrant_host}:{settings.qdrant_port}"
        )
        self.collection_name = settings.collection_name
        self.embedder = Embedder()

    def retrieve(self, query: str, top_k: int = 20) -> List[Dict]:
        """Retrieve top-k relevant documents."""
        query_embedding = self.embedder.embed(query)

        search_result = self.client.query_points(
            collection_name=self.collection_name,
            query=query_embedding,
            limit=top_k,
            with_payload=True
        )

        results = []
        for hit in search_result.points:
            if hit.payload:
                results.append({
                    "content": hit.payload.get("content", ""),
                    "title": hit.payload.get("title", ""),
                    "doc_id": hit.payload.get("doc_id", ""),
                    "chunk_index": hit.payload.get("chunk_index", 0),
                    "score": hit.score,
                })

        return results

    def add_chunks(self, chunks: List[Dict], doc_id: str, source_file: str):
        """Add document chunks to Qdrant."""
        points = []
        for chunk in chunks:
            embedding = self.embedder.embed(chunk["content"])
            point_id = f"{doc_id}_{chunk['chunk_index']}"
            points.append(PointStruct(
                id=point_id,
                vector=embedding,
                payload={
                    "doc_id": doc_id,
                    "chunk_index": chunk["chunk_index"],
                    "content": chunk["content"],
                    "title": chunk.get("title", ""),
                    "source_file": source_file,
                }
            ))

        self.client.upsert(
            collection_name=self.collection_name,
            points=points
        )

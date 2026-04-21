from typing import List, Dict
import dashscope
from dashscope import TextReRank as TextRerank

from chat_service.config import get_settings

settings = get_settings()


class DashScopeReranker:
    """Reranker using DashScope text-rerank model."""

    def __init__(self):
        self.model = settings.rerank_model
        self.api_key = settings.dashscope_api_key
        dashscope.api_key = self.api_key

    def rerank(
        self,
        query: str,
        documents: List[str],
        top_n: int = 5
    ) -> List[Dict]:
        """Rerank documents by relevance to query."""
        if not documents:
            return []

        response = TextRerank.call(
            model=self.model,
            query=query,
            documents=documents,
            top_n=top_n
        )

        results = []
        if response.status_code == 200:
            for item in response.output.results:
                results.append({
                    "index": item.index,
                    "content": documents[item.index],
                    "score": item.relevance_score,
                })
        else:
            # Fallback: return original order
            for i, doc in enumerate(documents[:top_n]):
                results.append({
                    "index": i,
                    "content": doc,
                    "score": 1.0 - (i * 0.1),
                })

        return results

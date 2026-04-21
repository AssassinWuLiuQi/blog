import pytest
from chat_service.rag.reranker import DashScopeReranker


def test_reranker_init():
    reranker = DashScopeReranker()
    assert reranker.model == "text-rerank"

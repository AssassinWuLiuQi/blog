import pytest
from chat_service.rag.retriever import RAGRetriever


def test_retriever_retrieve():
    retriever = RAGRetriever()
    results = retriever.retrieve("test query", top_k=5)
    assert isinstance(results, list)

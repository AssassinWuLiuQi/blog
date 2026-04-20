import pytest
from chat_service.rag.embedder import Embedder


def test_embed_single_text():
    embedder = Embedder()
    result = embedder.embed("hello world")
    assert isinstance(result, list)
    assert len(result) == 384  # all-MiniLM-L6-v2 dimension


def test_embed_multiple_texts():
    embedder = Embedder()
    result = embedder.embed_batch(["hello", "world"])
    assert len(result) == 2
    assert len(result[0]) == 384

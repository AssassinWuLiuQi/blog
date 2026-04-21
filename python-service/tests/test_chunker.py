import pytest
from chat_service.rag.chunker import parse_document, chunk_by_headings


def test_parse_txt():
    content = b"Hello world\n\nThis is a test."
    result = parse_document(content, "test.txt")
    assert "Hello world" in result
    assert "This is a test" in result

def test_parse_unsupported_format():
    content = b"fake content"
    with pytest.raises(ValueError, match="Unsupported file format"):
        parse_document(content, "test.xyz")

def test_parse_pdf_unsupported():
    content = b"fake pdf content"
    with pytest.raises(ValueError, match="Unsupported file format"):
        parse_document(content, "test.xyz")

def test_parse_docx_unsupported():
    with pytest.raises(ValueError, match="Unsupported file format"):
        parse_document(b"fake", "test.doc")


def test_chunk_by_headings():
    text = """# 标题一

这是第一段内容。

## 副标题

这是第二段内容。

【中文标题】

这是第三段内容。"""

    chunks = chunk_by_headings(text)
    assert len(chunks) == 3
    assert chunks[0]["title"] == "# 标题一"
    assert chunks[0]["content"].startswith("# 标题一")
    assert chunks[2]["title"] == "【中文标题】"


def test_chunk_by_headings_with_custom_pattern():
    text = """第1章 开始

这是第一章的内容。

第2章 继续

这是第二章的内容。"""

    chunks = chunk_by_headings(text, heading_pattern=r'^第.+章')
    assert len(chunks) == 2
    assert chunks[0]["title"] == "第1章 开始"
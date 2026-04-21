import re
from typing import List, Dict


def parse_pdf(content: bytes) -> str:
    """Extract text from PDF using pdfplumber."""
    import pdfplumber
    from io import BytesIO

    text_parts = []
    with pdfplumber.open(BytesIO(content)) as pdf:
        for page in pdf.pages:
            page_text = page.extract_text()
            if page_text:
                text_parts.append(page_text)
    return '\n\n'.join(text_parts)


def parse_docx(content: bytes) -> str:
    """Extract text from DOCX using python-docx."""
    from docx import Document
    from io import BytesIO

    doc = Document(BytesIO(content))
    paragraphs = [p.text for p in doc.paragraphs if p.text.strip()]
    return '\n\n'.join(paragraphs)


def parse_document(content: bytes, filename: str) -> str:
    """Parse document content and extract plain text."""
    ext = filename.lower().split('.')[-1]

    if ext == 'txt':
        return content.decode('utf-8', errors='replace')

    if ext == 'pdf':
        return parse_pdf(content)

    if ext == 'docx':
        return parse_docx(content)

    raise ValueError(f"Unsupported file format: {ext}")


def chunk_by_headings(
    text: str,
    heading_pattern: str = r'^#{1,6}\s+|^【[^】]+】|^\[[^\]]+\]$',
    max_chars_per_chunk: int = 500
) -> List[Dict]:
    """Chunk text by headings, each heading + its content becomes a chunk."""
    # Split by double newlines (paragraphs)
    blocks = re.split(r'\n\n+', text)

    chunks = []
    current_title = "无标题"
    current_lines = []

    for block in blocks:
        block = block.strip()
        if not block:
            continue

        # Check if this block is a heading
        if re.match(heading_pattern, block):
            # Save previous chunk
            if current_lines:
                content = '\n'.join([current_title] + current_lines).strip()
                if content:
                    chunks.append({
                        "content": content,
                        "title": current_title,
                    })

            current_title = block
            current_lines = []
        else:
            current_lines.append(block)

    # Save last chunk
    if current_lines:
        content = '\n'.join([current_title] + current_lines).strip()
        if content:
            chunks.append({
                "content": content,
                "title": current_title,
            })

    # Add metadata
    for i, chunk in enumerate(chunks):
        chunk["chunk_index"] = i

    return chunks
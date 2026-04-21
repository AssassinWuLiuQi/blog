from typing import List, Optional
from contextlib import asynccontextmanager
from fastapi import FastAPI, HTTPException, BackgroundTasks, UploadFile, File, Form, Query
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from sse_starlette.sse import EventSourceResponse
from langchain_core.messages import HumanMessage, AIMessage
import json
import hashlib

from chat_service.graph.chat_graph import chat_graph
from chat_service.services.retrieval import RetrievalService
from chat_service.config import get_settings
from chat_service.rag import (
    chunk_by_headings,
    parse_document,
    RAGRetriever,
    DashScopeReranker,
)

settings = get_settings()


class ChatRequest(BaseModel):
    messages: List[dict]
    user_id: str = "anonymous"
    session_id: str = "default"
    response_mode: str = "streaming"


class ChatResponse(BaseModel):
    content: str
    session_id: str


retrieval_service = RetrievalService()
rag_retriever = RAGRetriever()
rag_reranker = DashScopeReranker()


@asynccontextmanager
async def lifespan(app: FastAPI):
    retrieval_service.ensure_collection()
    yield


app = FastAPI(
    title="Chat Service",
    description="Smart customer service with LangGraph + MiniMax",
    version="1.0.0",
    lifespan=lifespan
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/health")
async def health_check():
    return {"status": "healthy"}


@app.post("/api/chat/stream")
async def stream_chat(request: ChatRequest):
    """Stream chat response using SSE."""

    def generate():
        langchain_messages = []
        for msg in request.messages:
            if msg.get("role") == "user":
                langchain_messages.append(HumanMessage(content=msg["content"]))
            elif msg.get("role") == "assistant":
                langchain_messages.append(AIMessage(content=msg["content"]))

        initial_state = {
            "messages": langchain_messages,
            "user_id": request.user_id,
            "session_id": request.session_id,
            "retrieved_docs": [],
            "response_mode": request.response_mode,
            "current_response": ""
        }

        try:
            for event in chat_graph.stream(initial_state):
                if "chat_completion" in event:
                    response = event["chat_completion"].get("current_response", "")
                    if response:
                        yield {
                            "event": "message",
                            "data": json.dumps({"content": response})
                        }

            yield {
                "event": "done",
                "data": json.dumps({"session_id": request.session_id})
            }

        except Exception as e:
            yield {
                "event": "error",
                "data": json.dumps({"error": str(e)})
            }

    return EventSourceResponse(generate())


@app.post("/api/chat/sync")
async def sync_chat(request: ChatRequest) -> ChatResponse:
    """Synchronous chat response."""
    langchain_messages = []
    for msg in request.messages:
        if msg.get("role") == "user":
            langchain_messages.append(HumanMessage(content=msg["content"]))
        elif msg.get("role") == "assistant":
            langchain_messages.append(AIMessage(content=msg["content"]))

    initial_state = {
        "messages": langchain_messages,
        "user_id": request.user_id,
        "session_id": request.session_id,
        "retrieved_docs": [],
        "response_mode": "sync",
        "current_response": ""
    }

    result = await chat_graph.ainvoke(initial_state)

    return ChatResponse(
        content=result.get("current_response", ""),
        session_id=request.session_id
    )


@app.post("/api/knowledge/add")
async def add_knowledge(content: str, metadata: Optional[dict] = None):
    """Add document to knowledge base."""
    try:
        retrieval_service.add_document(content, metadata or {})
        return {"status": "success", "message": "Document added"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/rag/ingest")
async def ingest_document(
    file: UploadFile = File(...),
    title: Optional[str] = Form(None),
    heading_pattern: Optional[str] = Form(None),
    max_chars: Optional[int] = Form(None),
):
    """Upload and ingest a document."""
    # Validate file size
    settings = get_settings()
    max_size = settings.max_file_size_mb * 1024 * 1024
    content = await file.read()
    if len(content) > max_size:
        raise HTTPException(
            status_code=413,
            detail=f"File too large. Max size: {settings.max_file_size_mb}MB"
        )

    # Validate file type
    ext = file.filename.lower().split('.')[-1]
    if ext not in ['txt', 'pdf', 'docx']:
        raise HTTPException(
            status_code=400,
            detail="Unsupported file format. Use txt, pdf, or docx"
        )

    # Generate doc_id
    doc_id = hashlib.md5(content).hexdigest()[:12]

    # Use title from form or filename
    doc_title = title or file.filename

    # Parse document
    try:
        text = parse_document(content, file.filename)
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))

    # Chunk text
    pattern = heading_pattern or settings.heading_pattern
    chars_limit = max_chars or settings.max_chars_per_chunk
    chunks = chunk_by_headings(text, heading_pattern=pattern, max_chars_per_chunk=chars_limit)

    # Add metadata
    for chunk in chunks:
        chunk["doc_id"] = doc_id
        chunk["source_file"] = file.filename

    # Store in Qdrant
    rag_retriever.add_chunks(chunks, doc_id, file.filename)

    return {
        "status": "success",
        "doc_id": doc_id,
        "chunks": len(chunks),
        "title": doc_title,
    }


@app.get("/api/rag/search")
async def search_documents(
    q: str = Query(..., description="Search query"),
    top_k: int = Query(20, description="Number of initial results"),
    top_n: int = Query(5, description="Number of final results after rerank"),
):
    """Search documents with reranking."""
    settings = get_settings()

    # Vector search
    results = rag_retriever.retrieve(q, top_k=top_k)

    if not results:
        return {"query": q, "results": []}

    # Rerank
    documents = [r["content"] for r in results]
    reranked = rag_reranker.rerank(q, documents, top_n=top_n)

    # Build response with original metadata
    doc_index_map = {i: r for i, r in enumerate(results)}
    response_results = []
    for item in reranked:
        original = doc_index_map.get(item["index"], {})
        response_results.append({
            "content": item["content"],
            "title": original.get("title", ""),
            "score": item["score"],
            "doc_id": original.get("doc_id", ""),
            "chunk_index": original.get("chunk_index", 0),
        })

    return {"query": q, "results": response_results}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)

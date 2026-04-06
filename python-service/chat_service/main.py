from typing import List, Optional
from contextlib import asynccontextmanager
from fastapi import FastAPI, HTTPException, BackgroundTasks
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from sse_starlette.sse import EventSourceResponse
from langchain_core.messages import HumanMessage, AIMessage
import json

from chat_service.graph.chat_graph import chat_graph
from chat_service.services.retrieval import RetrievalService
from chat_service.config import get_settings

settings = get_settings()


class ChatRequest(BaseModel):
    messages: List[dict]
    user_id: str = "anonymous"
    session_id: str = "default"
    use_voice: bool = False
    response_mode: str = "streaming"


class ChatResponse(BaseModel):
    content: str
    session_id: str
    use_voice: bool = False


retrieval_service = RetrievalService()


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
            "use_voice": request.use_voice,
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
        "use_voice": request.use_voice,
        "response_mode": "sync",
        "current_response": ""
    }

    result = await chat_graph.ainvoke(initial_state)

    return ChatResponse(
        content=result.get("current_response", ""),
        session_id=request.session_id,
        use_voice=request.use_voice
    )


@app.post("/api/knowledge/add")
async def add_knowledge(content: str, metadata: Optional[dict] = None):
    """Add document to knowledge base."""
    try:
        retrieval_service.add_document(content, metadata or {})
        return {"status": "success", "message": "Document added"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)

# Plan B: Python LangGraph Service

## Overview

Build the Python LangGraph service that handles AI dialogue workflow: knowledge retrieval → query routing → MiniMax chat completion → optional TTS synthesis. Uses FastAPI for HTTP/SSE endpoints and Qdrant for vector storage.

**Files to create:**

```
python-service/
├── chat_service/
│   ├── __init__.py
│   ├── main.py                    # FastAPI app entry
│   ├── config.py                  # Settings
│   ├── state.py                   # ChatState TypedDict
│   ├── graph/
│   │   ├── __init__.py
│   │   ├── chat_graph.py          # LangGraph workflow
│   │   ├── nodes.py               # Node functions
│   │   └── edges.py               # Conditional edges
│   ├── services/
│   │   ├── __init__.py
│   │   ├── minimax_client.py      # MiniMax API client
│   │   └── retrieval.py           # Vector retrieval
│   └── knowledge_base/
│       ├── __init__.py
│       └── embedder.py            # Text embedding
├── requirements.txt
├── Dockerfile
└── .env.example
```

---

## Task 1: Project Setup

**Files:**
- `python-service/requirements.txt` (create)
- `python-service/.env.example` (create)

- [ ] **Step 1: Create requirements.txt**

```txt
langgraph==0.2.55
langchain-core==0.3.24
langchain-community==0.3.12
fastapi==0.115.6
uvicorn[standard]==0.34.0
sse-starlette==2.2.0
qdrant-client==1.12.1
httpx==0.28.1
pydantic==2.10.4
pydantic-settings==2.7.0
python-dotenv==1.0.1
sentence-transformers==3.3.1
numpy==1.26.4
```

- [ ] **Step 2: Create .env.example**

```env
MINIMAX_API_KEY=your_api_key_here
MINIMAX_API_URL=https://api.minimaxi.com/v1
QDRANT_HOST=localhost
QDRANT_PORT=6333
COLLECTION_NAME=blog_knowledge
LOG_LEVEL=INFO
```

- [ ] **Step 3: Create empty init files**

```bash
mkdir -p python-service/chat_service/graph
mkdir -p python-service/chat_service/services
mkdir -p python-service/chat_service/knowledge_base
touch python-service/__init__.py
touch python-service/chat_service/__init__.py
touch python-service/chat_service/graph/__init__.py
touch python-service/chat_service/services/__init__.py
touch python-service/chat_service/knowledge_base/__init__.py
```

- [ ] **Step 4: Commit**

```bash
git add python-service/
git commit -m "feat(chat): add Python service project structure"
```

---

## Task 2: Config Module

**File:** `python-service/chat_service/config.py`

**Purpose:** Application settings loaded from environment variables.

- [ ] **Step 1: Create config.py**

```python
from pydantic_settings import BaseSettings
from functools import lru_cache


class Settings(BaseSettings):
    minimax_api_key: str = ""
    minimax_api_url: str = "https://api.minimaxi.com/v1"
    qdrant_host: str = "localhost"
    qdrant_port: int = 6333
    collection_name: str = "blog_knowledge"
    log_level: str = "INFO"
    embedding_model: str = "sentence-transformers/all-MiniLM-L6-v2"

    class Config:
        env_file = ".env"
        extra = "allow"


@lru_cache()
def get_settings() -> Settings:
    return Settings()
```

- [ ] **Step 2: Test import**

Run: `cd python-service && pip install pydantic pydantic-settings -q && python -c "from chat_service.config import get_settings; print(get_settings().minimax_api_url)"`
Expected: `https://api.minimaxi.com/v1`

- [ ] **Step 3: Commit**

```bash
git add python-service/chat_service/config.py
git commit -m "feat(chat): add config module with pydantic settings"
```

---

## Task 3: State Definition

**File:** `python-service/chat_service/state.py`

**Purpose:** Define the ChatState TypedDict used throughout the LangGraph workflow.

- [ ] **Step 1: Create state.py**

```python
from typing import TypedDict, List, Optional
from langchain_core.messages import BaseMessage


class ChatState(TypedDict):
    """State passed between nodes in the chat graph."""

    messages: List[BaseMessage]
    user_id: str
    session_id: str
    retrieved_docs: List[str]
    use_voice: bool
    response_mode: str  # 'streaming' | 'sync'
    current_response: str
```

- [ ] **Step 2: Test import**

Run: `cd python-service && python -c "from chat_service.state import ChatState; print('OK')"`
Expected: `OK`

- [ ] **Step 3: Commit**

```bash
git add python-service/chat_service/state.py
git commit -m "feat(chat): add ChatState TypedDict"
```

---

## Task 4: MiniMax Client

**File:** `python-service/chat_service/services/minimax_client.py`

**Purpose:** Client for MiniMax Chat Completion API with streaming support.

- [ ] **Step 1: Create minimax_client.py**

```python
import httpx
import json
from typing import AsyncIterator, List, Dict, Any
from langchain_core.messages import HumanMessage, AIMessage, SystemMessage
from chat_service.config import get_settings

settings = get_settings()


class MiniMaxClient:
    """Client for MiniMax Chat Completion API."""

    def __init__(self, api_key: str = None):
        self.api_key = api_key or settings.minimax_api_key
        self.base_url = settings.minimax_api_url

    def _build_messages(self, state_messages: List, system_prompt: str, context_docs: List[str]) -> List[Dict]:
        """Build message list for API request."""
        messages = [{"role": "system", "content": system_prompt}]

        if context_docs:
            context_text = "\n\n---\n\n相关知识：\n" + "\n".join(context_docs)
            messages.append({"role": "system", "content": f"[知识库检索结果]\n{context_text}"})

        for msg in state_messages:
            if isinstance(msg, HumanMessage):
                messages.append({"role": "user", "content": msg.content})
            elif isinstance(msg, AIMessage):
                messages.append({"role": "assistant", "content": msg.content})

        return messages

    async def chat(
        self,
        messages: List,
        system_prompt: str = "",
        context_docs: List[str] = None,
        stream: bool = True
    ) -> AsyncIterator[str]:
        """Send chat completion request with optional streaming."""
        url = f"{self.base_url}/chat/completions"

        payload = {
            "model": "MiniMax-Text-01",
            "messages": self._build_messages(messages, system_prompt, context_docs or []),
            "stream": stream,
            "temperature": 0.7,
            "max_tokens": 2048
        }

        headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json"
        }

        async with httpx.AsyncClient(timeout=120.0) as client:
            async with client.stream("POST", url, json=payload, headers=headers) as response:
                response.raise_for_status()
                async for line in response.aiter_lines():
                    if line.startswith("data: "):
                        data = line[6:]
                        if data == "[DONE]":
                            break
                        try:
                            chunk = json.loads(data)
                            delta = chunk.get("choices", [{}])[0].get("delta", {})
                            content = delta.get("content", "")
                            if content:
                                yield content
                        except json.JSONDecodeError:
                            continue

    async def chat_sync(self, messages: List, system_prompt: str = "", context_docs: List[str] = None) -> str:
        """Send chat completion request without streaming."""
        url = f"{self.base_url}/chat/completions"

        payload = {
            "model": "MiniMax-Text-01",
            "messages": self._build_messages(messages, system_prompt, context_docs or []),
            "stream": False,
            "temperature": 0.7,
            "max_tokens": 2048
        }

        headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json"
        }

        async with httpx.AsyncClient(timeout=120.0) as client:
            response = await client.post(url, json=payload, headers=headers)
            response.raise_for_status()
            data = response.json()
            return data["choices"][0]["message"]["content"]
```

- [ ] **Step 2: Test import**

Run: `cd python-service && python -c "from chat_service.services.minimax_client import MiniMaxClient; print('OK')"`
Expected: `OK`

- [ ] **Step 3: Commit**

```bash
git add python-service/chat_service/services/minimax_client.py
git commit -m "feat(chat): add MiniMax client with streaming support"
```

---

## Task 5: Retrieval Service

**File:** `python-service/chat_service/services/retrieval.py`

**Purpose:** Vector similarity search using Qdrant.

- [ ] **Step 1: Create retrieval.py**

```python
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
```

- [ ] **Step 2: Test import (without Qdrant running)**

Run: `cd python-service && python -c "from chat_service.services.retrieval import RetrievalService; print('OK')"`
Expected: `OK`

- [ ] **Step 3: Commit**

```bash
git add python-service/chat_service/services/retrieval.py
git commit -m "feat(chat): add retrieval service with Qdrant"
```

---

## Task 6: Graph Nodes

**File:** `python-service/chat_service/graph/nodes.py`

**Purpose:** Define the node functions for the LangGraph workflow.

- [ ] **Step 1: Create nodes.py**

```python
from typing import Dict, Any
from langchain_core.messages import HumanMessage
from chat_service.state import ChatState
from chat_service.services.retrieval import RetrievalService
from chat_service.services.minimax_client import MiniMaxClient

retrieval_service = RetrievalService()
minimax_client = MiniMaxClient()

SYSTEM_PROMPT = """你是一个博客网站的智能客服助手，名为"小博"。

你的特点:
- 语气亲切友好，像博客作者的朋友
- 熟悉博客内容，能回答关于博客文章的问题
- 能为用户提供帮助和建议

回答原则:
1. 如果问题与博客内容相关，基于知识库回答
2. 如果无法找到确切答案，诚实地说明并提供一般性建议
3. 保持回答简洁明了，避免过于冗长
4. 在适当时候使用友好的语气
"""


async def retrieve_knowledge(state: ChatState) -> Dict[str, Any]:
    """Retrieve relevant documents from knowledge base."""
    user_message = state["messages"][-1].content if state["messages"] else ""

    try:
        docs = retrieval_service.retrieve(user_message, top_k=5)
    except Exception:
        docs = []

    return {"retrieved_docs": docs}


async def chat_completion(state: ChatState) -> Dict[str, Any]:
    """Generate response using MiniMax."""
    messages_for_api = [m for m in state["messages"] if isinstance(m, HumanMessage)]

    if state["response_mode"] == "streaming":
        response_content = ""
        async for chunk in minimax_client.chat(
            messages=messages_for_api,
            system_prompt=SYSTEM_PROMPT,
            context_docs=state.get("retrieved_docs", []),
            stream=True
        ):
            response_content += chunk

        return {"current_response": response_content}

    else:
        response_content = await minimax_client.chat_sync(
            messages=messages_for_api,
            system_prompt=SYSTEM_PROMPT,
            context_docs=state.get("retrieved_docs", [])
        )

        return {"current_response": response_content}


async def synthesize_voice(state: ChatState) -> Dict[str, Any]:
    """Prepare for voice synthesis (placeholder for TTS integration)."""
    return {"use_voice": True}
```

- [ ] **Step 2: Test import**

Run: `cd python-service && python -c "from chat_service.graph.nodes import retrieve_knowledge, chat_completion; print('OK')"`
Expected: `OK`

- [ ] **Step 3: Commit**

```bash
git add python-service/chat_service/graph/nodes.py
git commit -m "feat(chat): add LangGraph nodes"
```

---

## Task 7: Graph Edges

**File:** `python-service/chat_service/graph/edges.py`

**Purpose:** Define conditional edges for routing in the graph.

- [ ] **Step 1: Create edges.py**

```python
from chat_service.state import ChatState


def route_query(state: ChatState) -> str:
    """Route to appropriate node based on query type."""
    return "chat_completion"


def should_synthesize_voice(state: ChatState) -> str:
    """Determine if voice synthesis is needed."""
    if state.get("use_voice", False):
        return "synthesize_voice"
    return "end"
```

- [ ] **Step 2: Test import**

Run: `cd python-service && python -c "from chat_service.graph.edges import route_query; print('OK')"`
Expected: `OK`

- [ ] **Step 3: Commit**

```bash
git add python-service/chat_service/graph/edges.py
git commit -m "feat(chat): add LangGraph conditional edges"
```

---

## Task 8: Chat Graph

**File:** `python-service/chat_service/graph/chat_graph.py`

**Purpose:** Build and compile the LangGraph workflow.

- [ ] **Step 1: Create chat_graph.py**

```python
from langgraph.graph import StateGraph, END
from chat_service.state import ChatState
from chat_service.graph.nodes import retrieve_knowledge, chat_completion, synthesize_voice
from chat_service.graph.edges import route_query, should_synthesize_voice


def build_chat_graph() -> StateGraph:
    """Build the chat workflow graph."""
    workflow = StateGraph(ChatState)

    workflow.add_node("retrieve_knowledge", retrieve_knowledge)
    workflow.add_node("chat_completion", chat_completion)
    workflow.add_node("synthesize_voice", synthesize_voice)

    workflow.set_entry_point("retrieve_knowledge")
    workflow.add_edge("retrieve_knowledge", "route_query")

    workflow.add_conditional_edges(
        "route_query",
        route_query,
        {
            "chat_completion": "chat_completion",
        }
    )

    workflow.add_conditional_edges(
        "chat_completion",
        should_synthesize_voice,
        {
            "synthesize_voice": "synthesize_voice",
            "end": END
        }
    )

    workflow.add_edge("synthesize_voice", END)

    return workflow.compile()


chat_graph = build_chat_graph()
```

- [ ] **Step 2: Test import**

Run: `cd python-service && python -c "from chat_service.graph.chat_graph import chat_graph; print('OK')"`
Expected: `OK`

- [ ] **Step 3: Commit**

```bash
git add python-service/chat_service/graph/chat_graph.py
git commit -m "feat(chat): add compiled LangGraph workflow"
```

---

## Task 9: FastAPI Main App

**File:** `python-service/chat_service/main.py`

**Purpose:** FastAPI application with SSE endpoint for streaming chat.

- [ ] **Step 1: Create main.py**

```python
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
```

- [ ] **Step 2: Test import**

Run: `cd python-service && python -c "from chat_service.main import app; print('OK')"`
Expected: `OK`

- [ ] **Step 3: Commit**

```bash
git add python-service/chat_service/main.py
git commit -m "feat(chat): add FastAPI app with SSE streaming endpoint"
```

---

## Task 10: Dockerfile

**File:** `python-service/Dockerfile`

**Purpose:** Containerize the Python service for easy deployment.

- [ ] **Step 1: Create Dockerfile**

```dockerfile
FROM python:3.11-slim

WORKDIR /app

RUN apt-get update && apt-get install -y \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY chat_service/ ./chat_service/

ENV PYTHONPATH=/app

EXPOSE 8000

CMD ["uvicorn", "chat_service.main:app", "--host", "0.0.0.0", "--port", "8000"]
```

- [ ] **Step 2: Commit**

```bash
git add python-service/Dockerfile
git commit -m "feat(chat): add Dockerfile for Python service"
```

---

## Verification

After all tasks complete:

```bash
cd python-service
pip install -r requirements.txt -q
python -c "from chat_service.main import app; print('FastAPI app loaded OK')"
```

Expected: `FastAPI app loaded OK`

---

## Next Steps

After Plan B completes:
- Plan C (Frontend) can start using the API endpoints defined in Task 9

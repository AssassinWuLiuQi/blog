# Smart Customer Service Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a multi-scenario intelligent customer service system based on MiniMax Chat API with LangGraph workflow orchestration, supporting text/voice dialogue, hybrid knowledge base, and persistent conversation history.

**Architecture:** Spring Boot backend manages auth and conversation history (MySQL). Python LangGraph service handles AI dialogue workflow (retrieval → routing → chat completion → optional TTS). Vue frontend provides chat UI with streaming responses.

**Tech Stack:** Spring Boot 3.3 (Java 21), Python 3.11 + LangGraph, Qdrant (vector store), MiniMax Chat API + TTS API, Vue 3 + Vite + Tailwind CSS

---

## Implementation Order

This plan is divided into 3 sub-plans that can be executed in parallel after the backend schema:

1. **[Plan A: Backend](2026-04-06-smart-customer-service-plan-a-backend.md)** — Spring Boot Chat API + Database Schema
2. **[Plan B: Python Service](2026-04-06-smart-customer-service-plan-b-python-service.md)** — LangGraph Service + Vector Store
3. **[Plan C: Frontend](2026-04-06-smart-customer-service-plan-c-frontend.md)** — Vue Chat Components

---

## Prerequisite: Database Schema

Execute this DDL in MySQL before starting any plan:

```sql
-- Chat sessions table
CREATE TABLE IF NOT EXISTS chat_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_name VARCHAR(255) DEFAULT '新对话',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Chat messages table
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL COMMENT 'user|assistant|system',
    content TEXT NOT NULL,
    tokens_used INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES chat_sessions(id) ON DELETE CASCADE
);

-- Index for faster queries
CREATE INDEX idx_chat_messages_session ON chat_messages(session_id);
CREATE INDEX idx_chat_sessions_user ON chat_sessions(user_id);
```

---

## Execution Flow

```
Week 1: Plan A (Backend)
├── Task 1-3: Database entities & repositories
└── Task 4-6: Chat service & controller + Security config

Week 2: Plan B (Python Service) - Can start in parallel after Task 2
├── Task 1-4: LangGraph setup + MiniMax integration
├── Task 5-7: Vector store + retrieval
└── Task 8-10: Streaming API + TTS integration

Week 3: Plan C (Frontend) - Can start in parallel
├── Task 1-3: Chat components
├── Task 4-6: Voice mode + streaming
└── Task 7-8: Integration + testing
```

---

## Key Decisions Made

| Item | Decision | Rationale |
|------|----------|-----------|
| Vector DB | Qdrant | Lightweight, local deployable, good LangChain integration |
| Python API | FastAPI | Async support, SSE built-in, auto OpenAPI docs |
| Embedding | MiniMax embedding API | Consistent with LLM provider |
| TTS | Reuse existing Spring Boot TTS | Avoid duplication |
| Auth | JWT from Spring Boot | Pass user token to Python service |

---

## Dependencies Between Plans

- **Plan A must complete Task 3** (repositories exist) before Plan B can start Task 3
- **Plan B must complete Task 4** (API endpoint known) before Plan C can start Task 1
- All plans can run in parallel after the prerequisite schema is created

---

## Verification Commands

After all plans complete:

```bash
# Backend
cd backend && mvn clean compile -q

# Python Service
cd python-service && pip install -r requirements.txt && python -c "from chat_service.main import app; print('OK')"

# Frontend
cd frontend && npm install && npm run build
```

---

## Post-Implementation Checklist

- [ ] All three sub-plans completed
- [ ] Backend compiles without errors
- [ ] Python service starts without import errors
- [ ] Frontend builds successfully
- [ ] End-to-end chat flow works (send message → get response)
- [ ] Streaming response displays correctly
- [ ] Voice mode plays audio response
- [ ] Conversation history persists after page refresh

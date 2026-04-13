# 智能客服系统设计方案

> **日期:** 2026-04-06
> **状态:** 已批准

---

## 1. 项目概述

**目标:** 基于 MiniMax Chat API 实现多场景智能客服系统，支持网站在线客服和内部知识库问答。

**核心功能:**
- 多轮对话（基础多轮，5-10轮上下文记忆）
- 混合知识库（静态文档 + 博客内容）
- 双模式交互（文字 + 语音）
- 对话历史持久化存储
- 博客作者化身风格

---

## 2. 系统架构

```
┌─────────────────────────────────────────────────────────────────┐
│                         Vue Frontend                            │
│                    (客服对话界面 + 语音模式)                       │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Spring Boot Backend                           │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────┐  │
│  │ Auth API │  │Article API│  │Chat API │  │TTS (已有)    │  │
│  └──────────┘  └──────────┘  └──────────┘  └──────────────┘  │
│                                                              │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │              对话历史管理 (MySQL)                         │ │
│  └──────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                  Python LangGraph Service                        │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    ChatGraph (LangGraph)                  │  │
│  │  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌───────────┐  │  │
│  │  │retrieve │→ │  route  │→ │  chat   │→ │ synthesize│  │  │
│  │  └─────────┘  └─────────┘  └─────────┘  └───────────┘  │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              向量数据库 (Qdrant / pgvector)               │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      MiniMax API                                 │
│            (Chat Completion + TTS Speech)                        │
└─────────────────────────────────────────────────────────────────┘
```

---

## 3. 技术选型

### Backend (Spring Boot)
- **现有框架:** Spring Boot 3.3 + Java 21
- **数据库:** MySQL (存储对话历史)
- **认证:** JWT (已有)
- **通信:** REST API + SSE (流式响应)

### AI Layer (Python)
- **框架:** LangGraph
- **向量库:** Qdrant (轻量级，可本地部署) 或 PostgreSQL + pgvector
- **LLM:** MiniMax Chat API (`/v1/chat/completions`)
- **TTS:** 复用现有 Spring Boot TTS 服务

### Frontend (Vue)
- **现有框架:** Vue 3 + Vite + Tailwind CSS
- **新增:** 客服对话组件、语音输入/输出组件

---

## 4. 核心模块设计

### 4.1 对话历史存储 (MySQL)

```sql
-- 对话会话表
CREATE TABLE chat_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_name VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 对话消息表
CREATE TABLE chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,  -- 'user' | 'assistant' | 'system'
    content TEXT NOT NULL,
    tokens_used INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES chat_sessions(id)
);
```

### 4.2 Python LangGraph 工作流

```python
# 状态定义
class ChatState(TypedDict):
    messages: List[BaseMessage]           # 对话历史
    user_id: str                          # 用户ID
    session_id: str                       # 会话ID
    retrieved_docs: List[Document]        # 检索到的文档
    use_voice: bool                       # 是否使用语音模式
    response_mode: str                     # 'streaming' | 'sync'

# 节点定义
nodes:
  - retrieve_knowledge: 检索知识库
  - route_query: 路由查询类型
  - chat_completion: 调用 MiniMax 生成回复
  - synthesize_voice: 语音合成（可选）

# 边定义
edges:
  - start → retrieve_knowledge
  - retrieve_knowledge → route_query
  - route_query → chat_completion (normal)
  - chat_completion → synthesize_voice (if use_voice)
  - synthesize_voice → end
```

### 4.3 知识库构建

**博客内容:**
- 定时将博客文章内容分块 (chunking)
- 通过嵌入模型生成向量
- 存储到向量数据库

**静态文档:**
- 支持上传 PDF/Markdown
- 自动解析 + 分块 + 向量化

**检索流程:**
1. 用户 query → 嵌入向量
2. 向量相似度检索 (top-k)
3. 结合检索结果构建 prompt

---

## 5. API 设计

### 5.1 Spring Boot → Python Service

```
POST /api/chat/sessions          # 创建新会话
GET  /api/chat/sessions          # 获取用户会话列表
GET  /api/chat/sessions/{id}     # 获取会话详情
DELETE /api/chat/sessions/{id}   # 删除会话

POST /api/chat/sessions/{id}/messages    # 发送消息
GET  /api/chat/sessions/{id}/messages    # 获取历史消息

POST /api/chat/stream             # 流式对话 (SSE)
```

### 5.2 Python Service → MiniMax

```
POST /v1/chat/completions         # 文本对话
POST /v1/t2a_v2                   # 语音合成 (复用)
```

---

## 6. 前端组件

### 6.1 客服对话组件
- 消息列表（用户/AI 消息区分）
- 输入框 + 发送按钮
- 语音输入按钮
- 流式输出显示
- 历史会话切换

### 6.2 语音模式
- 语音输入 (Web Speech API)
- AI 回复语音播放
- 波形动画显示

---

## 7. System Prompt 设计

```python
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

当前对话上下文中的用户信息: {user_context}
"""
```

---

## 8. 数据流

### 8.1 文字对话流程

```
1. 用户发送消息 → Spring Boot
2. Spring Boot → Python Service (HTTP)
3. Python Service:
   a. retrieve_knowledge (检索知识库)
   b. chat_completion (调用 MiniMax)
4. Python Service → Spring Boot (SSE 流式响应)
5. Spring Boot → 前端 (SSE)
6. 前端显示流式输出
7. 消息保存到 MySQL
```

### 8.2 语音对话流程

```
1. 用户语音输入 → 前端 (Web Speech API)
2. 语音转文字 → Spring Boot
3. Spring Boot → Python Service
4. Python Service → MiniMax (文本回复)
5. Python Service → Spring Boot (文本)
6. Spring Boot → TTS Service (已有)
7. TTS 流式音频 → 前端播放
8. 保存对话到 MySQL
```

---

## 9. 待确认事项

- [ ] 向量数据库选择: Qdrant vs pgvector
- [ ] Python 服务部署方式: Docker / 直接运行
- [ ] 知识库文档格式支持范围
- [ ] 会话过期策略（多久未访问算新会话）

---

## 10. 实现优先级

### Phase 1: 核心对话
1. MySQL 对话历史表设计
2. Python LangGraph 基础框架
3. MiniMax Chat API 集成
4. 基础对话 API (同步)
5. 流式对话 (SSE)

### Phase 2: 知识库
6. 博客内容向量化和检索
7. 静态文档上传和解析
8. RAG 检索增强

### Phase 3: 语音 + 前端
9. 前端客服组件开发
10. 语音输入/输出集成
11. 语音模式完整流程

### Phase 4: 优化
12. 多轮对话优化
13. 会话管理增强
14. 监控和日志

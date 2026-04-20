# RAG 技术方案

## 概述

基于 FastAPI + LangGraph + Qdrant + DashScope 构建通用 RAG（检索增强生成）架构，服务于博客智能客服场景。

## 一、整体架构

```
chat_service/
├── rag/                      # RAG 核心模块
│   ├── __init__.py
│   ├── chunker.py            # 文档解析 + 标题分块
│   ├── embedder.py           # 向量化（复用现有 RetrievalService）
│   ├── retriever.py          # 向量检索
│   └── reranker.py           # DashScope 重排序
├── services/
│   └── retrieval.py          # 现有 RetrievalService（向量化存储）
├── graph/
│   └── nodes.py              # 新增 rag_node
└── main.py                   # 新增 /api/rag/ingest

外部依赖：
- Qdrant (82.156.199.60:10007) — 向量数据库
- DashScope — text-rerank 重排序
- sentence-transformers — 文本嵌入
```

## 二、文档处理流程（Ingest）

```
用户上传文档 (txt/pdf/docx, ≤50MB)
        │
        ▼
    文档解析
    提取纯文本
        │
        ▼
    标题分块
    (按 heading_pattern 识别标题 + 内容合并)
        │
        ▼
    逐块向量化
    (sentence-transformers/all-MiniLM-L6-v2)
        │
        ▼
    存入 Qdrant
    (collection: blog_knowledge)
```

## 三、RAG 检索流程（Query）

```
用户查询
    │
    ▼
查询向量化
(sentence-transformers/all-MiniLM-L6-v2)
    │
    ▼
向量检索 Qdrant
Top-K = 20（多召回）
    │
    ▼
重排序
(DashScope text-rerank → Top-N = 5)
    │
    ▼
返回相关文档片段 + 元数据
给 LLM 作为上下文
```

## 四、分块策略

### 4.1 chunk_by_headings

按标题分块，每个标题及其下的内容合并为一个 chunk。

**参数：**
- `text`: 原始文本
- `heading_pattern`: 标题识别正则，默认 `r'^#{1,6}\s+|^【[^】]+】|^\[[^\]]+\]$'`
- `max_chars_per_chunk`: 最大字符数，默认 500（超出不拆，保留语义完整）

**逻辑：**
1. 按 `\n\n` 切分文本段落
2. 匹配 `heading_pattern` 的行识别为标题
3. 每个标题 + 其后续内容合并为一个 chunk
4. 超过 `max_chars` 也不拆分，保持语义完整

**metadata 结构：**
```python
{
    "doc_id": str,           # 文档唯一ID
    "chunk_index": int,      # chunk 序号
    "title": str,            # 所属标题
    "source_file": str,      # 原始文件名
}
```

## 五、接口设计

### 5.1 POST /api/rag/ingest

上传文档并入库。

**请求 (multipart/form-data)：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | binary | 是 | 文档文件 |
| title | string | 否 | 文档标题（默认用文件名） |
| heading_pattern | string | 否 | 标题识别正则 |
| max_chars | int | 否 | 最大字符数，默认 500 |

**响应：**
```json
{
  "status": "success",
  "doc_id": "abc123",
  "chunks": 15
}
```

**限制：**
- 文件大小：≤50MB
- 支持格式：txt, pdf, docx

### 5.2 GET /api/rag/search

搜索测试（调试用）。

**请求参数：**
| 参数 | 类型 | 说明 |
|------|------|------|
| q | string | 查询文本 |
| top_k | int | 召回数量，默认 20 |
| top_n | int | 重排后返回数量，默认 5 |

**响应：**
```json
{
  "query": "...",
  "results": [
    {
      "content": "...",
      "title": "标题",
      "score": 0.95,
      "doc_id": "abc123",
      "chunk_index": 3
    }
  ]
}
```

## 六、数据模型

### 6.1 Qdrant Collection

**collection_name:** `blog_knowledge`

**向量配置：**
- size: 384 (all-MiniLM-L6-v2)
- distance: COSINE

**payload schema：**
```python
{
    "doc_id": str,
    "chunk_index": int,
    "content": str,
    "title": str,
    "source_file": str,
}
```

## 七、配置项

### 7.1 config.py 新增

```python
class Settings(BaseSettings):
    # 现有配置...

    # DashScope Rerank
    dashscope_api_key: str = ""
    rerank_model: str = "text-rerank"
    rerank_top_k: int = 20
    rerank_top_n: int = 5

    # RAG
    heading_pattern: str = r'^#{1,6}\s+|^【[^】]+】|^\[[^\]]+\]$'
    max_chars_per_chunk: int = 500
    max_file_size_mb: int = 50
```

## 八、集成方式

RAG 逻辑嵌入 chat_service，作为 LangGraph 的一个节点：

```
chat_graph 流程：
retrieve_knowledge → chat_completion → END

其中 retrieve_knowledge 节点内部：
1. 调用 rag/retriever 检索相关文档
2. 调用 rag/reranker 重排序
3. 返回 top-N 文档作为上下文
```

## 九、文件结构

```
chat_service/
├── rag/
│   ├── __init__.py
│   ├── chunker.py          # 文档解析 + 标题分块
│   ├── embedder.py         # 向量化（复用 RetrievalService）
│   ├── retriever.py        # 向量检索
│   └── reranker.py         # DashScope 重排序
├── services/
│   └── retrieval.py         # 现有 RetrievalService
├── graph/
│   └── nodes.py            # retrieve_knowledge 节点调用 RAG
├── main.py                 # 新增 /api/rag/* 接口
└── config.py               # 新增 DashScope 配置
```

## 十、技术栈

| 组件 | 技术 |
|------|------|
| Web 框架 | FastAPI + uvicorn |
| 工作流 | LangGraph |
| 向量数据库 | Qdrant |
| 嵌入模型 | sentence-transformers/all-MiniLM-L6-v2 |
| 重排序 | DashScope text-rerank |
| LLM | MiniMax |
| 文档解析 | pdfplumber (PDF), python-docx (Word) |

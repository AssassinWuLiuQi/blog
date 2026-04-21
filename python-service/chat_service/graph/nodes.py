from typing import Dict, Any
from langchain_core.messages import HumanMessage
from chat_service.state import ChatState
from chat_service.services.retrieval import RetrievalService
from chat_service.services.minimax_client import MiniMaxClient
from chat_service.rag.retriever import RAGRetriever
from chat_service.rag.reranker import DashScopeReranker
from chat_service.config import get_settings

retrieval_service = RetrievalService()
minimax_client = MiniMaxClient()
rag_retriever = RAGRetriever()
rag_reranker = DashScopeReranker()

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
    """Retrieve relevant documents from knowledge base with reranking."""
    user_message = state["messages"][-1].content if state["messages"] else ""

    settings = get_settings()

    try:
        # Vector search
        docs = rag_retriever.retrieve(user_message, top_k=settings.rerank_top_k)

        if not docs:
            return {"retrieved_docs": []}

        # Rerank
        documents = [d["content"] for d in docs]
        reranked = rag_reranker.rerank(
            user_message,
            documents,
            top_n=settings.rerank_top_n
        )

        # Extract final documents with metadata
        doc_map = {i: d for i, d in enumerate(docs)}
        final_docs = []
        for item in reranked:
            original = doc_map.get(item["index"], {})
            final_docs.append({
                "content": item["content"],
                "title": original.get("title", ""),
                "score": item["score"],
            })

        return {"retrieved_docs": final_docs}

    except Exception:
        return {"retrieved_docs": []}


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



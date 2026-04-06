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

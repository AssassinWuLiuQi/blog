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

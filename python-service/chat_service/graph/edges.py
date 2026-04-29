from chat_service.state import ChatState


def route_query(state: ChatState) -> str:
    """Route to appropriate node based on query type."""
    return "chat_completion"



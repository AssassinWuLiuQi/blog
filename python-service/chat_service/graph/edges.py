from chat_service.state import ChatState


def route_query(state: ChatState) -> str:
    """Route to appropriate node based on query type."""
    return "chat_completion"


def should_synthesize_voice(state: ChatState) -> str:
    """Determine if voice synthesis is needed."""
    if state.get("use_voice", False):
        return "synthesize_voice"
    return "end"

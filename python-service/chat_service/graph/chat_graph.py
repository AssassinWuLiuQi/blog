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

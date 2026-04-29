from langgraph.graph import StateGraph, END
from chat_service.state import ChatState
from chat_service.graph.nodes import retrieve_knowledge, chat_completion
from chat_service.graph.edges import route_query


def build_chat_graph() -> StateGraph:
    """Build the chat workflow graph."""
    workflow = StateGraph(ChatState)

    workflow.add_node("retrieve_knowledge", retrieve_knowledge)
    workflow.add_node("chat_completion", chat_completion)
    workflow.set_entry_point("retrieve_knowledge")

    workflow.add_conditional_edges(
        "retrieve_knowledge",
        route_query,
        {
            "chat_completion": "chat_completion",
        }
    )

    workflow.add_edge("chat_completion", END)

    return workflow.compile()


chat_graph = build_chat_graph()

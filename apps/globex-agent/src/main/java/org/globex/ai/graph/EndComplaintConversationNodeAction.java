package org.globex.ai.graph;

import dev.langchain4j.data.message.ToolExecutionResultMessage;
import org.bsc.langgraph4j.action.NodeAction;

import java.util.List;
import java.util.Map;

public class EndComplaintConversationNodeAction {

    public static NodeAction<State> get(String tool, String routingTarget) {
        return state -> {
            List<ToolExecutionResultMessage> toolExecutionResultMessages = state.toolExecutionResultMessages();
            boolean found = toolExecutionResultMessages.stream().anyMatch(t -> t.toolName().equals(tool));
            if (found) {
                return Map.of("routing_target", routingTarget);
            }
            return Map.of();
        };

    }
}

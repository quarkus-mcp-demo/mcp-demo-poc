package org.globex.ai.graph;

import org.bsc.langgraph4j.action.NodeAction;
import org.globex.ai.agent.ConversationChatMemory;
import org.globex.ai.model.AssistantMessage;
import org.globex.ai.model.Message;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class LlmNodeWithChatMemoryAction {

    public static NodeAction<State> get(BiFunction<String, String, String> llmAction) {
        return get(llmAction, (r,state) -> state);
    }

    public static NodeAction<State> get(BiFunction<String, String, String> llmAction, StateAction stateAction) {
        return state -> {
            Message lastHumanMessage = state.lastHumanMessage();
            String request = lastHumanMessage == null ? "" : lastHumanMessage.content();
            String threadId = state.value("thread_id").orElse("default").toString();
            String response = llmAction.apply(request, threadId);
            Map<String, Object> updateState = state.addMessage(new AssistantMessage(response));
            return stateAction.apply(response, updateState);
        };
    }

}

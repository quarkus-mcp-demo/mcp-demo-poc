package org.globex.ai.graph;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.ChatMemory;
import org.bsc.langgraph4j.action.NodeAction;
import org.globex.ai.agent.ConversationChatMemory;
import org.globex.ai.model.AssistantMessage;
import org.globex.ai.model.Message;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class LlmNodeWithChatMemoryAction {

    public static NodeAction<State> get(BiFunction<String, String, String> llmAction, ConversationChatMemory conversationChatMemory) {
        return get(llmAction, conversationChatMemory, (r,state) -> state);
    }

    public static NodeAction<State> get(BiFunction<String, String, String> llmAction, ConversationChatMemory conversationChatMemory, StateAction stateAction) {
        return state -> {
            Message lastHumanMessage = state.lastHumanMessage();
            String request = lastHumanMessage == null ? "" : lastHumanMessage.content();
            String threadId = state.value("thread_id").orElse("default").toString();
            String response = llmAction.apply(request, threadId);
            Map<String, Object> updateState = state.addMessage(new AssistantMessage(response));

            ChatMemory chatMemory = conversationChatMemory.get(threadId);
            if (chatMemory != null) {
                List<ToolExecutionResultMessage> toolExecutionResultMessages = state.toolExecutionResultMessages();
                List<ChatMessage> messages = chatMemory.messages();
                messages.forEach(message -> {
                    if (message instanceof ToolExecutionResultMessage toolExecutionResultMessage) {
                        boolean found = toolExecutionResultMessages.stream().anyMatch(t -> t.id().equals(toolExecutionResultMessage.id()));
                        if (!found) {
                            state.addToolExecutionResultMessage(toolExecutionResultMessage, updateState);
                        }
                    }
                });
            }
            return stateAction.apply(response, updateState);
        };
    }

}

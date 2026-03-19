package org.globex.ai.graph;

import dev.langchain4j.data.message.ToolExecutionResultMessage;
import io.vertx.core.json.JsonObject;
import org.bsc.langgraph4j.state.AgentState;
import org.globex.ai.model.AssistantMessage;
import org.globex.ai.model.HumanMessage;
import org.globex.ai.model.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class State extends AgentState {

    public State(Map<String, Object> initData) {
        super(initData);
    }

    public List<Message> messages() {
        List<String> serializedMessages = (List<String>) value("messages").orElse(new ArrayList<>());
        return serializedMessages.stream().map(this::fromJson).toList();
    }

    public Map<String, Object> addMessage(Message message, Map<String, Object> newState) {
        List<String> serializedMessages = (List<String>) value("messages").orElse(new ArrayList<>());
        serializedMessages.add(toJson(message));
        return State.updateState(newState, Map.of("messages", serializedMessages), null);
    }

    public Map<String, Object> addMessage(Message message) {
        List<String> serializedMessages = (List<String>) value("messages").orElse(new ArrayList<>());
        serializedMessages.add(toJson(message));
        return State.updateState(this, Map.of("messages", serializedMessages), null);
    }

    public List<Message> humanMessages() {
        return messages().stream().filter(message -> message instanceof HumanMessage).toList();
    }

    public List<Message> aiMessages() {
        return messages().stream().filter(message -> message instanceof AssistantMessage).toList();
    }

    public Message lastHumanMessage() {
        List<Message> humanMessages = humanMessages();
        if (humanMessages.isEmpty()) {
            return null;
        } else {
            return humanMessages.getLast();
        }
    }

    public Message lastAIMessage() {
        if (aiMessages().isEmpty()) {
            return null;
        } else {
            return aiMessages().getLast();
        }
    }

    public List<ToolExecutionResultMessage> toolExecutionResultMessages() {
        List<String> serializedMessages = (List<String>) value("tool_execution_results").orElse(new ArrayList<>());
        return serializedMessages.stream().map(this::toolExecutionResultMessagefromJson).toList();
    }

    public Map<String, Object> addToolExecutionResultMessage(ToolExecutionResultMessage message) {
        List<String> serializedMessages = (List<String>) value("tool_execution_results").orElse(new ArrayList<>());
        serializedMessages.add(toolExecutionResultMessageToJson(message));
        return State.updateState(this, Map.of("tool_execution_results", serializedMessages), null);
    }

    public Map<String, Object> addToolExecutionResultMessage(ToolExecutionResultMessage message, Map<String, Object> newState) {
        List<String> serializedMessages = (List<String>) value("tool_execution_results").orElse(new ArrayList<>());
        serializedMessages.add(toolExecutionResultMessageToJson(message));
        return State.updateState(newState, Map.of("tool_execution_results", serializedMessages), null);
    }

    public ToolExecutionResultMessage lastToolExecutionResultMessage() {
        if (toolExecutionResultMessages().isEmpty()) {
            return null;
        } else {
            return toolExecutionResultMessages().getLast();
        }
    }

    String toJson(Message message) {
        JsonObject json = new JsonObject();
        json.put("content", message.content());
        if (message instanceof HumanMessage) {
            json.put("role", "user");
        }  else if (message instanceof AssistantMessage) {
            json.put("role", "assistant");
        }
        return json.encode();
    }

    Message fromJson(String serialized) {
        JsonObject json = new JsonObject(serialized);
        String role = json.getString("role");
        String content = json.getString("content");
        if ("user".equals(role)) {
            return new HumanMessage(content);
        } else {
            return new AssistantMessage(content);
        }
    }

    ToolExecutionResultMessage toolExecutionResultMessagefromJson(String serialized) {
        JsonObject json = new JsonObject(serialized);
        return ToolExecutionResultMessage.builder()
                .id(json.getString("id"))
                .toolName(json.getString("toolName"))
                .attributes(json.getJsonObject("attributes").getMap())
                .text(json.getString("text"))
                .build();
    }

    String toolExecutionResultMessageToJson(ToolExecutionResultMessage message) {
        JsonObject json = new JsonObject();
        json.put("id", message.id());
        json.put("toolName", message.toolName());
        json.put("attributes", new JsonObject(message.attributes()));
        json.put("text", message.text());
        return json.encode();
    }
}

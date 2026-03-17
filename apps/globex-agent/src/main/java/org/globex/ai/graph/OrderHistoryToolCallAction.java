package org.globex.ai.graph;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.service.tool.ToolExecutionResult;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.bsc.langgraph4j.action.NodeAction;
import org.globex.ai.model.AssistantMessage;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

public class OrderHistoryToolCallAction {

    public static NodeAction<State> get(McpClient client) {
        return state -> {
            ToolExecutionRequest toolExecutionRequest = ToolExecutionRequest.builder()
                    .id("tool-" + UUID.randomUUID())
                    .name("get_order_history")
                    .arguments(new JsonObject().encode())
                    .build();
            ToolExecutionResult toolExecutionResult = client.executeTool(toolExecutionRequest);
            //todo exception handling
            ToolExecutionResultMessage toolExecutionResultMessage = ToolExecutionResultMessage.from(toolExecutionRequest,
                    toolExecutionResult.resultText());
            Map<String, Object> updateState = state.addToolExecutionResultMessage(toolExecutionResultMessage);
            updateState.put("order_history", toolExecutionResult.resultText());
            AssistantMessage assistantMessage = new AssistantMessage(buildResponse(toolExecutionResult.resultText()));
            state.addMessage(assistantMessage, updateState);
            return updateState;
        };
    }

    static String buildResponse(String result) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Hello! I'm here to help you file a product complaint.").append(System.lineSeparator());
        stringBuilder.append("These are your recent orders:").append(System.lineSeparator()).append(System.lineSeparator());
        stringBuilder.append(formatOrderHistory(result)).append(System.lineSeparator());
        stringBuilder.append("For which product would you like to file a complaint?");
        return stringBuilder.toString();
    }

    static String formatOrderHistory(String result) {
        StringBuilder stringBuilder = new StringBuilder();
        JsonObject json = new JsonObject(result);
        JsonArray jsonArray = json.getJsonArray("orders");
        final int[] orderCounter = {1};
        jsonArray.stream().forEach(order -> {
            JsonObject jsonOrder = (JsonObject) order;
            stringBuilder.append(orderCounter[0]).append(". Order ").append(jsonOrder.getLong("id")).append(" from ")
                    .append(formatDateTime(Instant.parse(jsonOrder.getString("timestamp")), "yyyy-MM-dd"))
                    .append(":").append(System.lineSeparator())
                    .append("   Products:").append(System.lineSeparator());
            JsonArray lineItemArray = jsonOrder.getJsonArray("lineItems");
            final int[] lineItemCounter = {1};
            lineItemArray.stream().forEach(lineItem -> {
                JsonObject lineItemJson = (JsonObject) lineItem;
                stringBuilder.append("      ").append(lineItemCounter[0]).append(". ").append(lineItemJson.getString("productCode"))
                        .append(" - ").append(lineItemJson.getString("productName")).append(System.lineSeparator());
                lineItemCounter[0]++;
            });
        });
        return stringBuilder.toString();
    }

    static String formatDateTime(Instant instant, String pattern) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return formatter.format(localDateTime);
    }
}

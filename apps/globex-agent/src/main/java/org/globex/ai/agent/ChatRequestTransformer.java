package org.globex.ai.agent;

import dev.langchain4j.model.chat.request.ChatRequest;

import java.util.function.UnaryOperator;

public class ChatRequestTransformer {

    public static UnaryOperator<ChatRequest> overrideTemperature(Double temperature) {
        return chatRequest -> ChatRequest.builder()
                .temperature(temperature)
                .frequencyPenalty(chatRequest.frequencyPenalty())
                .messages(chatRequest.messages())
                .maxOutputTokens(chatRequest.maxOutputTokens())
                .modelName(chatRequest.modelName())
                .presencePenalty(chatRequest.presencePenalty())
                .responseFormat(chatRequest.responseFormat())
                .stopSequences(chatRequest.stopSequences())
                .toolChoice(chatRequest.toolChoice())
                .toolSpecifications(chatRequest.toolSpecifications())
                .topK(chatRequest.topK())
                .topP(chatRequest.topP())
                .build();
    }

}

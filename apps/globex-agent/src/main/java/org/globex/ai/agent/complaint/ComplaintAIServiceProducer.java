package org.globex.ai.agent.complaint;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import io.quarkiverse.langchain4j.ModelName;
import io.quarkiverse.langchain4j.mcp.runtime.McpClientName;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.globex.ai.agent.AIServiceConfigs;
import org.globex.ai.agent.ConversationChatMemoryProviderSupplier;

import static org.globex.ai.agent.ChatRequestTransformer.overrideTemperature;

@ApplicationScoped
public class ComplaintAIServiceProducer {

    @Inject
    @ModelName("complaint")
    ChatModel chatModel;

    @Inject
    @McpClientName("complaints")
    McpClient client;

    @Inject
    AIServiceConfigs aiServiceConfigs;

    @Inject
    ConversationChatMemoryProviderSupplier chatMemoryProviderSupplier;

    @Produces
    public ProductSelectionAIService provideProductSelectionAIService() {
        return AiServices.builder(ProductSelectionAIService.class)
                .chatModel(chatModel)
                .chatRequestTransformer(overrideTemperature(aiServiceConfigs.aiServiceConfigs().get("product_selection").temperature()))
                .build();
    }

    @Produces
    public HandleProductNotSelectedAIService provideHandleProductNotSelectedAIService() {
        return AiServices.builder(HandleProductNotSelectedAIService.class)
                .chatModel(chatModel)
                .chatRequestTransformer(overrideTemperature(aiServiceConfigs.aiServiceConfigs().get("product_not_selected").temperature()))
                .build();
    }

    @Produces
    public ComplaintAIService provideComplaintAIService() {
        McpToolProvider toolProvider = McpToolProvider.builder()
                .mcpClients(client)
                .filterToolNames("create_complaint")
                .build();

        return AiServices.builder(ComplaintAIService.class)
                .chatModel(chatModel)
                .chatRequestTransformer(overrideTemperature(aiServiceConfigs.aiServiceConfigs().get("complaint").temperature()))
                .chatMemoryProvider(chatMemoryProviderSupplier.get())
                .toolProvider(toolProvider)
                .build();
    }
}

package org.globex.ai.agent;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import io.quarkiverse.langchain4j.ModelName;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

@ApplicationScoped
public class RoutingAIServiceProducer {

    @Inject
    @ModelName("routing")
    ChatModel chatModel;

    @Inject
    AIServiceConfigs aiServiceConfigs;

    @Produces
    public IdentifyNeedAIService provideIdentifyNeedAIService() {
        return AiServices.builder(IdentifyNeedAIService.class)
                .chatModel(chatModel)
                .chatRequestTransformer(ChatRequestTransformer.overrideTemperature(aiServiceConfigs.aiServiceConfigs().get("identify-need").temperature()))
                .build();
    }

    @Produces
    public HandleOtherRequestAIService provideHandleOtherRequestAIService() {
        return AiServices.builder(HandleOtherRequestAIService.class)
                .chatModel(chatModel)
                .chatRequestTransformer(ChatRequestTransformer.overrideTemperature(aiServiceConfigs.aiServiceConfigs().get("identify-need").temperature()))
                .build();
    }

    @Produces
    public ClassifyIntentAIService provideClassifyIntentAIService() {
        return AiServices.builder(ClassifyIntentAIService.class)
                .chatModel(chatModel)
                .chatRequestTransformer(ChatRequestTransformer.overrideTemperature(aiServiceConfigs.aiServiceConfigs().get("identify-need").temperature()))
                .build();
    }

}

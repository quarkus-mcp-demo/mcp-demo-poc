package org.globex.ai.agent;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.function.Supplier;

@Singleton
public class ConversationChatMemoryProviderSupplier implements Supplier<ChatMemoryProvider> {

    @Inject
    ConversationChatMemory sessionChatMemory;

    @Override
    public ChatMemoryProvider get() {
        return memoryId -> {
            if (memoryId == null) {
                return null;
            }
            ChatMemory chatMemory = sessionChatMemory.get(memoryId);
            if (chatMemory == null) {
                MessageWindowChatMemory messageWindowChatMemory =  MessageWindowChatMemory.builder()
                        .maxMessages(20)
                        .id(memoryId)
                        .chatMemoryStore(new InMemoryChatMemoryStore())
                        .build();
                sessionChatMemory.put((String) memoryId, messageWindowChatMemory);
                return messageWindowChatMemory;
            }
            return chatMemory;
        };
    }
}

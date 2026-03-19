package org.globex.ai.agent;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import jakarta.inject.Singleton;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class ConversationChatMemory {

    final Map<String, ChatMemory> chatMemoryMap = new HashMap<>();

    public void put(String sessionId, ChatMemory chatMemory) {
        this.chatMemoryMap.put(sessionId, chatMemory);
    }

    public ChatMemory get(Object memoryId) {
        ChatMemory chatMemory =  this.chatMemoryMap.get((String)memoryId);
        if (chatMemory == null) {
            MessageWindowChatMemory messageWindowChatMemory =  MessageWindowChatMemory.builder()
                    .maxMessages(20)
                    .id(memoryId)
                    .chatMemoryStore(new InMemoryChatMemoryStore())
                    .build();
            put((String) memoryId, messageWindowChatMemory);
            return messageWindowChatMemory;
        }
        return chatMemory;
    }
}

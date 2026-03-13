package org.globex.ai.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface HandleOtherRequestAIService {

    @SystemMessage("""
            You are a routing agent specializing in getting users to the correct specialist agent.
            Be helpful, friendly, and efficient in determining their needs.
            """)
    @UserMessage("""
            The user said: "{userMessage}"
            
            Their request doesn't clearly match our available services. Politely explain that you can currently help with:
            - Laptop refresh requests
            - Email address updates
            
            Ask them to clarify if they need help with one of these areas, or let them know you cannot help with their specific request.
            
            Be helpful and professional.
            """)
    String process(String userMessage);

}

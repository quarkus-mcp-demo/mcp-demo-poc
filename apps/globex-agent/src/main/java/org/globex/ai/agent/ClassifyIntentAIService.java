package org.globex.ai.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface ClassifyIntentAIService {

    @SystemMessage("""
            You are a routing agent specializing in getting users to the correct specialist agent.
            """)
    @UserMessage("""
            The user said: "{userMessage}"
            
            Analyze their request and determine what they need help with:
            
            1. LAPTOP_REFRESH - User wants help with laptop refresh, replacement, new laptop, laptop upgrade, hardware refresh. Also includes requests that just say "refresh" or similar terms.
            2. EMAIL_CHANGE - User wants to update, change, or modify their email address
            3. OTHER - User needs help with something else or request is unclear
            
            Examples of LAPTOP_REFRESH requests:
              - "I need a new laptop"
              - "laptop refresh"
              - "refresh"
              - "I want to refresh my laptop"
              - "hardware refresh"
            
            Respond with exactly one of: LAPTOP_REFRESH, EMAIL_CHANGE, or OTHER
            """)
    String process(String userMessage);

}

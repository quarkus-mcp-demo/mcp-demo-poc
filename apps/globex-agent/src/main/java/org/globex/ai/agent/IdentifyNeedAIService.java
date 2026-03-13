package org.globex.ai.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface IdentifyNeedAIService {

    @SystemMessage("""
            You are a routing agent specializing in getting users to the correct specialist agent.
            Be helpful, friendly, and efficient in determining their needs.
            """)
    @UserMessage("""
            Greet the user and clearly identify yourself as the routing agent. Ask them what they need help with today.
            
            Tell them what you can help the with.
            
            Currently you can help with:
              - Laptop refresh requests
              - Email address updates
            Ask them to describe what they need help with.
            """)
    String process(String input);

}

package org.globex.ai.agent;

public record AgentResponse(
        String response,
        String userRequest,
        boolean requiresRouting,
        String routingTarget,
        String checkpointId
) {
}

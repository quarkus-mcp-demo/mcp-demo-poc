package org.globex.ai.agent;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.state.StateSnapshot;
import org.globex.ai.graph.State;
import org.globex.ai.model.HumanMessage;
import org.globex.ai.model.Message;

import java.util.Map;
import java.util.Optional;

public class BaseAgent {

    public AgentResponse invokeAgent(AgentRequest request, CompiledGraph<State> graph) {

        NodeOutput<State> output;
        RunnableConfig config;

        if (request.checkpointId() == null) {
            config = RunnableConfig.builder()
                    .threadId(request.threadId())
                    .streamMode(CompiledGraph.StreamMode.SNAPSHOTS)
                    .build();
            State state = new State(Map.of());
            Map<String, Object> updateState = state.addMessage(new HumanMessage(request.request()));
            updateState.put("thread_id", request.threadId());
            output = executeGraphAndFetchLastState(graph, config, updateState);
        } else {
            config = RunnableConfig.builder()
                    .threadId(request.threadId())
                    .checkPointId(request.checkpointId())
                    .streamMode(CompiledGraph.StreamMode.SNAPSHOTS)
                    .build();
            State state = graph.getState(config).state();
            Map<String, Object> updateState = state.addMessage(new HumanMessage(request.request()));

            try {
                config = graph.updateState(config, updateState);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            output = executeGraphAndFetchLastState(graph, config, null);
        }

        // Get last snapshot
        RunnableConfig lastConfig = graph.lastStateOf(config)
                .map(StateSnapshot::config)
                .orElse( null) ;

        State outputState = output.state();
        Message message = outputState.lastAIMessage();
        Message humanMessage = outputState.lastHumanMessage();
        String response = message == null ? "" : message.content();
        String userRequest = humanMessage == null? "" : humanMessage.content();
        Optional<String> routingTarget = outputState.value("routing_target");
        String checkpointId = lastConfig == null ? null : lastConfig.checkPointId().orElse(null);
        return routingTarget.map(target -> new AgentResponse(response, userRequest, true, target, checkpointId))
                .orElseGet(() -> new AgentResponse(response, userRequest, false, null, checkpointId));
    }

    private NodeOutput<State> executeGraphAndFetchLastState(CompiledGraph<State> graph, RunnableConfig config, Map<String, Object> input ) {

        // Get last state
        return graph.stream(input, config)
                .stream()
                .reduce((a, b) -> b)
                .orElseThrow();
    }

}

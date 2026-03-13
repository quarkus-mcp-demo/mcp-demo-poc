package org.globex.ai.graph;

import io.smallrye.common.annotation.Identifier;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.bsc.langgraph4j.*;
import org.bsc.langgraph4j.action.AsyncEdgeAction;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.checkpoint.PostgresSaver;
import org.bsc.langgraph4j.utils.EdgeMappings;
import org.globex.ai.agent.ClassifyIntentAIService;
import org.globex.ai.agent.HandleOtherRequestAIService;
import org.globex.ai.agent.IdentifyNeedAIService;
import org.globex.ai.persistence.PostgresqlConfig;

import java.sql.SQLException;
import java.util.Map;

@ApplicationScoped
public class RoutingAgentGraphProducer {

    @Inject
    PostgresqlConfig postgresqlConfig;

    @Inject
    IdentifyNeedAIService  identifyNeedAIService;

    @Inject
    HandleOtherRequestAIService handleOtherRequestAIService;

    @Inject
    ClassifyIntentAIService classifyIntentAIService;

    @Produces
    @Identifier("routing-agent")
    public CompiledGraph<State> buildGraph() {
        try {
            return compiledGraph();
        } catch (GraphStateException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    CompiledGraph<State> compiledGraph() throws GraphStateException, SQLException {
        AsyncNodeAction<State> greetAndIdentifyNeed = AsyncNodeAction.node_async(LlmNodeAction.get(s -> identifyNeedAIService.process(s)));
        AsyncNodeAction<State> handleOtherRequest = AsyncNodeAction.node_async(LlmNodeAction.get(s -> handleOtherRequestAIService.process(s)));
        AsyncNodeAction<State> classifyIntent = AsyncNodeAction.node_async(LlmNodeAction.get(s -> classifyIntentAIService.process(s), (value, state) -> {
            state.put("intent", value);
            return state;
        }));
        AsyncNodeAction<State> waitForUserInput =AsyncNodeAction.node_async(state -> Map.of());
        AsyncEdgeAction<State> handleIntent = AsyncEdgeAction.edge_async(state -> state.value("intent").orElse("OTHER").toString());

        StateGraph<State> graph = new StateGraph<>(State::new)
                .addNode("greet_and_identify_need",  greetAndIdentifyNeed)
                .addNode("handle_other_request", handleOtherRequest)
                .addNode("classify_intent", classifyIntent)
                .addNode("wait_for_input", waitForUserInput)
                .addEdge(GraphDefinition.START, "greet_and_identify_need")
                .addEdge("greet_and_identify_need", "wait_for_input")
                .addEdge("handle_other_request", "wait_for_input")
                .addEdge("wait_for_input", "classify_intent")
                .addConditionalEdges("classify_intent", handleIntent, EdgeMappings.builder()
                        .to(GraphDefinition.END,  "LAPTOP_REFRESH")
                        .to(GraphDefinition.END,  "EMAIL_CHANGE")
                        .to("handle_other_request", "OTHER")
                        .build());

        PostgresSaver saver = PostgresSaver.builder()
                .host(postgresqlConfig.host())
                .port(postgresqlConfig.port())
                .user(postgresqlConfig.username())
                .password(postgresqlConfig.password())
                .database(postgresqlConfig.database())
                .stateSerializer(graph.getStateSerializer())
                .createTables(false)
                .dropTablesFirst(false)
                .build();


        CompileConfig compileConfig = CompileConfig.builder()
                .checkpointSaver(saver)
                .interruptAfter("wait_for_input")
                .releaseThread(true)
                .build();

        return graph.compile(compileConfig);
    }

}

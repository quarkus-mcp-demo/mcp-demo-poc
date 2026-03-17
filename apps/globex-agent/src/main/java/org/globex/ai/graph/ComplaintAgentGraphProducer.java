package org.globex.ai.graph;

import dev.langchain4j.mcp.client.McpClient;
import io.quarkiverse.langchain4j.mcp.runtime.McpClientName;
import io.smallrye.common.annotation.Identifier;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.bsc.langgraph4j.*;
import org.bsc.langgraph4j.action.AsyncEdgeAction;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.checkpoint.PostgresSaver;
import org.bsc.langgraph4j.utils.EdgeMappings;
import org.globex.ai.agent.complaint.HandleProductNotSelectedAIService;
import org.globex.ai.agent.complaint.ProductSelectionAIService;
import org.globex.ai.persistence.PostgresqlConfig;

import java.sql.SQLException;
import java.util.Map;

import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@ApplicationScoped
public class ComplaintAgentGraphProducer {

    @Inject
    PostgresqlConfig postgresqlConfig;

    @Inject
    @McpClientName("globex-store")
    McpClient mcpClient;

    @Inject
    ProductSelectionAIService aiService;

    @Inject
    HandleProductNotSelectedAIService handleProductNotSelectedAIService;

    @Produces
    @Identifier("complaint-agent")
    public CompiledGraph<State> buildGraph() {
        try {
            return compiledGraph();
        } catch (GraphStateException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    CompiledGraph<State> compiledGraph() throws GraphStateException, SQLException {
        AsyncNodeAction<State> lookupOrderHistory = node_async(OrderHistoryToolCallAction.get(mcpClient));
        AsyncNodeAction<State> waitForUserInput = node_async(state -> Map.of());
        AsyncNodeAction<State> productSelection = node_async(ProductSelectionNodeAction.get((input, orderHistory) -> aiService.selectProduct(input, orderHistory)));
        AsyncNodeAction<State> handleProductNotSelected = node_async(LlmNodeAction.get(s -> handleProductNotSelectedAIService.handleRequest(s)));

        AsyncEdgeAction<State> handleProductSelection = edge_async(state -> state.value("product_selection").orElse("PRODUCT_NOT_SELECTED").toString());

        StateGraph<State> graph = new StateGraph<>(State::new)
                .addNode("lookup_order_history", lookupOrderHistory)
                .addNode("wait_for_input_product_selection", waitForUserInput)
                .addNode("product_selection", productSelection)
                .addNode("handle_product_not_selected", handleProductNotSelected)
                .addEdge(GraphDefinition.START, "lookup_order_history")
                .addEdge("lookup_order_history", "wait_for_input_product_selection")
                .addEdge("wait_for_input_product_selection", "product_selection")
                .addEdge("handle_product_not_selected", "wait_for_input_product_selection")
                .addConditionalEdges("product_selection", handleProductSelection, EdgeMappings.builder()
                        .to(GraphDefinition.END,  "PRODUCT_SELECTED")
                        .to("handle_product_not_selected", "PRODUCT_NOT_SELECTED")
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
                .interruptAfter("wait_for_input_product_selection")
                .releaseThread(true)
                .build();

        return graph.compile(compileConfig);

    }

}

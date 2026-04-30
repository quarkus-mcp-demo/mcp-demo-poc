package org.globex.retail.complaints.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkiverse.mcp.server.McpServer;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolCallException;
import jakarta.inject.Inject;
import org.globex.retail.complaints.service.DatabaseService;

import java.util.List;
import java.util.Map;

public class ComplaintsReadMcp {

    @Inject
    DatabaseService databaseService;

    @Inject
    ObjectMapper objectMapper;

    @McpServer("read")
    @Tool(name = "describe_tables", description = "Describe the database tables.")
    public String describeTable() {
        // restricted to the complaints table
        try {
            return objectMapper.writeValueAsString(databaseService.describeTable("complaints"));
        } catch (Exception e) {
            throw new ToolCallException("Failed to describe table: " + e.getMessage());
        }
    }

    @McpServer("read")
    @Tool(name = "list_tables", description = "List the tables in the database")
    public String listTables() {
        // restricted to the complaints table
        List<Map<String, String>> tables = databaseService.listTables().stream()
                .filter(stringStringMap -> "complaints".equalsIgnoreCase(stringStringMap.get("TABLE_NAME"))).toList();
        try {
            return objectMapper.writeValueAsString(tables);
        } catch (Exception e) {
            throw new ToolCallException("Failed to list tables: " + e.getMessage(), e);
        }

    }

    @McpServer("read")
    @Tool(name = "database_info", description = "Get information about the database. Run this before anything else to know the SQL dialect, keywords etc.")
    public String databaseInfo() {
        try {
            return objectMapper.writeValueAsString(databaseService.databaseInfo());
        } catch (Exception e) {
            throw new ToolCallException("Failed to get database info: " + e.getMessage(), e);
        }
    }

    @McpServer("read")
    @Tool(name = "read_query", description = "Execute a SELECT query on the database")
    public String readQuery(@ToolArg(description = "The SELECT SQL query to execute") String query) {
        // restricted to SELECT queries and the complaints table
        if (query == null || query.isEmpty()) {
            throw new ToolCallException("SQL query is null or empty");
        }
        if (!query.startsWith("SELECT")) {
            throw new ToolCallException("The query is not a valid SELECT query: " + query);
        }
        if (!query.toLowerCase().contains("complaints")) {
            throw new ToolCallException("SELECT queries are only allowed against the complaints table: " + query);
        }
        try {
            return objectMapper.writeValueAsString(databaseService.queryDatabase(query));
        } catch (Exception e) {
            throw new ToolCallException("Failed to execute query: " + e.getMessage(), e);
        }
    }

    @McpServer("read")
    @Tool(name = "get_last_1000_complaints", description = "Get the last 1000 complaints from the database")
    public String getLast1000Complaints() {
        String query = "SELECT * FROM complaints ORDER BY id DESC LIMIT 1000";
        try {
            return objectMapper.writeValueAsString(databaseService.queryDatabase(query));
        } catch (Exception e) {
            throw new ToolCallException("Failed to get recent complaints: " + e.getMessage(), e);
        }
    }
}

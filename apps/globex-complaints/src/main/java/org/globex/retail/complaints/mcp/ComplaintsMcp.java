package org.globex.retail.complaints.mcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolCallException;
import io.quarkus.logging.Log;
import io.vertx.core.http.HttpServerRequest;
import jakarta.inject.Inject;
import org.globex.retail.complaints.model.ComplaintDto;
import org.globex.retail.complaints.model.CreateComplaintRequest;
import org.globex.retail.complaints.model.UpdateComplaintRequest;
import org.globex.retail.complaints.service.ComplaintService;
import org.globex.retail.complaints.service.DatabaseService;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ComplaintsMcp {

    @Inject
    ComplaintService complaintService;

    @Inject
    DatabaseService databaseService;

    @Inject
    HttpServerRequest request;

    @Inject
    ObjectMapper objectMapper;

    @Tool(name = "create_complaint", description = "Create a product complaint")
    public String createComplaint(@ToolArg(description = "The order ID to which this complaint refers to") Long orderId,
                                  @ToolArg(description = "The productCode of the product to which this complaint refers to") String productCode,
                                  @ToolArg(description = "The issue type of the complaint. Accepted values: defect, missing_parts, wrong_item, performance, other ") String issueType,
                                  @ToolArg(description = "The severity of the issue or complaint. Accepted values: low, medium, high, or critical", required = false) String severity,
                                  @ToolArg(description = "The preferred resolution of the user for this complaint. Accepted values: refund, replacement, repair, other", required = false) String resolution,
                                  @ToolArg(description = "The description of the complaint") String description) {
        String userId = request.getHeader("X-User-Id");
        if (userId == null || userId.isEmpty()) {
            Log.error("Missing X-User-Id HTTP Header");
            throw new ToolCallException("Missing X-User-Id HTTP Header");
        }
        CreateComplaintRequest createComplaintRequest = new CreateComplaintRequest();
        createComplaintRequest.userId = userId;
        createComplaintRequest.orderId = orderId;
        createComplaintRequest.productCode = productCode;
        createComplaintRequest.issueType = issueType;
        createComplaintRequest.severity = severity;
        createComplaintRequest.complaint = description;
        createComplaintRequest.resolution = resolution;
        ComplaintDto complaintDTO = complaintService.createComplaint(createComplaintRequest);
        return String.valueOf(complaintDTO.id);
    }

    @Tool(name = "update_complaint", description = "Update an existing product complaint.")
    public String updateComplaint(@ToolArg(description = "The ID of the complaint") String id,
                                  @ToolArg(description = "The issue type of the complaint. Accepted values: defect, missing_parts, wrong_item, performance, other", required = false) String issueType,
                                  @ToolArg(description = "The severity of the issue or complaint. Accepted values: low, medium, high, or critical", required = false) String severity,
                                  @ToolArg(description = "The preferred resolution of the user for this complaint. Accepted values: refund, replacement, repair, other", required = false) String resolution,
                                  @ToolArg(description = "The description of the complaint", required = false) String description) {
        String userId = request.getHeader("X-User-Id");
        if (userId == null || userId.isEmpty()) {
            Log.error("Missing X-User-Id HTTP Header");
            throw new ToolCallException("Missing X-User-Id HTTP Header");
        }
        UpdateComplaintRequest updateComplaintRequest = new UpdateComplaintRequest();
        updateComplaintRequest.issueType = issueType;
        updateComplaintRequest.severity = severity;
        updateComplaintRequest.resolution = resolution;
        updateComplaintRequest.complaint = description;
        Optional<ComplaintDto> complaintDtoOptional = complaintService.updateComplaint(Long.valueOf(id), updateComplaintRequest);
        return complaintDtoOptional.map(dto -> String.valueOf(dto.id)).orElseThrow(() -> new RuntimeException("Complaint with id " + id + " not found"));
    }

    @Tool(name = "get_complaint", description = "Get product complaints for a given product and a given period, sorted by severity. Returns a list of JSON objects.")
    public String getComplaintsByProduct(@ToolArg(description = "The product code") String productId,
                                         @ToolArg(description = "The start date of the period. Format 'yyyy-MM-dd") String startDate,
                                         @ToolArg(description = "The end date of the period. Format 'yyyy-MM-dd") String endDate) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startLocalDate = LocalDate.parse(startDate, dateFormatter);
        LocalDate endLocalDate = LocalDate.parse(endDate, dateFormatter);
        OffsetDateTime start = startLocalDate.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime end = endLocalDate.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        List<ComplaintDto> complaintList = complaintService.findByProductCodeAndTimeRange(productId, start, end, true, null, null);
        try {
            return objectMapper.writeValueAsString(complaintList);
        } catch (JsonProcessingException e) {
            throw new ToolCallException("Failed to get complaint: " + e.getMessage(), e);
        }
    }

    @Tool(name = "describe_tables", description = "Describe the database tables.")
    public String describeTable() {
        // restricted to the complaints table
        try {
            return objectMapper.writeValueAsString(databaseService.describeTable("complaints"));
        } catch (Exception e) {
            throw new ToolCallException("Failed to describe table: " + e.getMessage());
        }
    }

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

    @Tool(name = "database_info", description = "Get information about the database. Run this before anything else to know the SQL dialect, keywords etc.")
    public String databaseInfo() {
        try {
            return objectMapper.writeValueAsString(databaseService.databaseInfo());
        } catch (Exception e) {
            throw new ToolCallException("Failed to get database info: " + e.getMessage(), e);
        }
    }

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
}

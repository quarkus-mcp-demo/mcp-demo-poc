package org.globex.mcp.store.mcp;

import io.quarkiverse.mcp.server.Tool;
import io.quarkus.logging.Log;
import io.vertx.core.http.HttpServerRequest;
import jakarta.inject.Inject;
import org.apache.commons.validator.routines.EmailValidator;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.globex.mcp.store.mcp.model.LineItem;
import org.globex.mcp.store.mcp.model.Order;
import org.globex.mcp.store.service.GlobexStoreApi;
import org.globex.mcp.store.service.model.Customer;

import java.util.List;

public class GlobexStoreMcp {

    @RestClient
    GlobexStoreApi globexStoreApi;

    @Inject
    HttpServerRequest request;

    @Tool(name = "get_order_history", description = "Retrieve a customer's order history based on their customer email")
    public List<Order> getOrderHistory() {
        String userId = request.getHeader("X-User-Id");
        if (userId == null || userId.isEmpty()) {
            Log.error("Missing X-User-Id HTTP Header");
            throw new RuntimeException("Missing X-User-Id HTTP Header");
        }
        EmailValidator emailValidator = EmailValidator.getInstance();
        Customer customer = null;
        if (emailValidator.isValid(userId)) {
            Log.infof("getOrderHistory Tool invoked for customer with email %s", userId);
            customer = globexStoreApi.getCustomerByUserEmail(userId);
        } else {
            Log.infof("getOrderHistory Tool invoked for customer with ID %s", userId);
            customer = globexStoreApi.getCustomerByUserId(userId);
        }
        if (customer == null) {
            return List.of();
        }
        List<org.globex.mcp.store.service.model.Order> orders = globexStoreApi.getOrdersByCustomerId(customer.getUserId());
        return orders.stream().map(o -> Order.builder()
                .withId(o.getId())
                .withCustomer(o.getCustomer())
                .withTimestamp(o.getTimestamp())
                .withOrderLineItems(o.getLineItems().stream().map(lineItem -> LineItem.builder()
                                .withProductCode(lineItem.getProduct())
                                .withProductName(globexStoreApi.getProductById(lineItem.getProduct()).getName())
                                .build())
                        .toList())
                .build()).toList();
    }

}

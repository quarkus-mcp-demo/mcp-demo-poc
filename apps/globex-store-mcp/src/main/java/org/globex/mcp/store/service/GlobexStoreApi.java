package org.globex.mcp.store.service;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.globex.mcp.store.service.model.Customer;
import org.globex.mcp.store.service.model.Order;
import org.globex.mcp.store.service.model.Product;

import java.util.List;

@RegisterRestClient(configKey = "globex-store")
@Path("/services")
public interface GlobexStoreApi {

    @GET
    @Path("/order/{customerId}/orders")
    List<Order> getOrdersByCustomerId(@PathParam("customerId") String customerId);

    @GET
    @Path("/customer/email/{email}")
    Customer getCustomerByUserEmail(@PathParam("email") String email);

    @GET
    @Path("/customer/id/{userId}")
    Customer getCustomerByUserId(@PathParam("userId") String userId);

    @GET
    @Path("/catalog/product/{id}")
    Product getProductById(@PathParam("id") String id);

}

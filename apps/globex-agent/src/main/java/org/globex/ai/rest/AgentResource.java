package org.globex.ai.rest;

import io.quarkus.logging.Log;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.globex.ai.service.AuthoritativeUserIdHolder;
import org.globex.ai.service.SessionManager;

@Path("/api/v1")
@Authenticated
public class AgentResource {

    @Inject
    JsonWebToken jwt;

    @Inject
    SessionManager sessionManager;

    @Inject
    AuthoritativeUserIdHolder authorativeUserIdHolder;

    @Path("/request")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> handleUserRequest(String request) {
        if (request == null || request.isEmpty()) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST).entity("Invalid data received. Null or empty request").build());
        }
        return Uni.createFrom().item(() -> request).emitOn(Infrastructure.getDefaultWorkerPool())
                .onItem().transform(r -> {
                    String userName = jwt.claim(Claims.preferred_username).orElse("").toString();
                    Log.infof("Agent request received: %s; userId: %s", r, userName);
                    authorativeUserIdHolder.setUserId(userName);
                    JsonObject jsonObject = new JsonObject(r);
                    return sessionManager.handleRequest(jsonObject.getString("request"), userName);
                })
                .onItem().transform(response -> Response.status(Response.Status.OK).entity(new JsonObject().put("response", response).encode()).build())
                .onFailure().recoverWithItem(throwable -> {
                    Log.error("Failed to handle request", throwable);
                    return Response.serverError().build();
                });
    }
}

package org.globex.retail.complaints.rest;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.globex.retail.complaints.model.ComplaintDto;
import org.globex.retail.complaints.model.CreateComplaintRequest;
import org.globex.retail.complaints.model.UpdateComplaintRequest;
import org.globex.retail.complaints.service.ComplaintService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Path("/api/v1/complaints")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ComplaintResource {

    @Inject
    ComplaintService complaintService;

    @POST
    public Uni<Response> createComplaint(CreateComplaintRequest request) {
        if (request == null) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST).build());
        }
        return Uni.createFrom().item(() -> request)
                .emitOn(Infrastructure.getDefaultWorkerPool())
                .onItem().transform(req -> complaintService.createComplaint(req))
                .onItem().transform(dto -> Response.status(Response.Status.CREATED).entity(dto).build())
                .onFailure().recoverWithItem(throwable -> {
                    Log.error("Failed to create complaint", throwable);
                    return Response.serverError().build();
                });
    }

    @PUT
    @Path("/{id}")
    public Uni<Response> updateComplaint(@PathParam("id") Long id, UpdateComplaintRequest request) {
        if (request == null) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST).build());
        }
        return Uni.createFrom().item(() -> id)
                .emitOn(Infrastructure.getDefaultWorkerPool())
                .onItem().transform(complaintId ->
                    complaintService.updateComplaint(complaintId, request)
                            .map(dto -> Response.ok(dto).build())
                            .orElse(Response.status(Response.Status.NOT_FOUND).build())
                )
                .onFailure().recoverWithItem(throwable -> {
                    Log.error("Failed to update complaint", throwable);
                    return Response.serverError().build();
                });
    }

    @GET
    @Path("/{id}")
    public Uni<Response> getComplaint(@PathParam("id") Long id) {
        return Uni.createFrom().item(() -> id)
                .emitOn(Infrastructure.getDefaultWorkerPool())
                .onItem().transform(complaintId ->
                    complaintService.findById(complaintId)
                            .map(dto -> Response.ok(dto).build())
                            .orElse(Response.status(Response.Status.NOT_FOUND).build())
                )
                .onFailure().recoverWithItem(throwable -> {
                    Log.error("Failed to get complaint", throwable);
                    return Response.serverError().build();
                });
    }

    @GET
    public Uni<Response> getAllComplaints() {
        return Uni.createFrom().item(() -> "")
                .emitOn(Infrastructure.getDefaultWorkerPool())
                .onItem().transform(ignore -> {
                    List<ComplaintDto> complaints = complaintService.findAll();
                    return Response.ok(complaints).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    Log.error("Failed to get all complaints", throwable);
                    return Response.serverError().build();
                });
    }

    @GET
    @Path("/user/{userId}")
    public Uni<Response> getComplaintsByUser(@PathParam("userId") String userId) {
        return Uni.createFrom().item(() -> userId)
                .emitOn(Infrastructure.getDefaultWorkerPool())
                .onItem().transform(uid -> {
                    List<ComplaintDto> complaints = complaintService.findByUserId(uid);
                    return Response.ok(complaints).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    Log.error("Failed to get complaints by user", throwable);
                    return Response.serverError().build();
                });
    }

    @GET
    @Path("/product/{productCode}")
    public Uni<Response> getComplaintsByProduct(@PathParam("productCode") String productCode) {
        return Uni.createFrom().item(() -> productCode)
                .emitOn(Infrastructure.getDefaultWorkerPool())
                .onItem().transform(code -> {
                    List<ComplaintDto> complaints = complaintService.findByProductCode(code);
                    return Response.ok(complaints).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    Log.error("Failed to get complaints by product", throwable);
                    return Response.serverError().build();
                });
    }

    @GET
    @Path("/product/{productCode}/timerange")
    public Uni<Response> getComplaintsByProductAndTimeRange(
            @PathParam("productCode") String productCode,
            @QueryParam("startTime") String startTime,
            @QueryParam("endTime") String endTime) {

        if (startTime == null || endTime == null) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"startTime and endTime query parameters are required\"}")
                    .build());
        }

        return Uni.createFrom().item(() -> productCode)
                .emitOn(Infrastructure.getDefaultWorkerPool())
                .onItem().transform(code -> {
                    OffsetDateTime start = OffsetDateTime.parse(startTime);
                    OffsetDateTime end = OffsetDateTime.parse(endTime);
                    List<ComplaintDto> complaints = complaintService.findByProductCodeAndTimeRange(code, start, end);
                    return Response.ok(complaints).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    Log.error("Failed to get complaints by product and time range", throwable);
                    return Response.serverError().build();
                });
    }
}

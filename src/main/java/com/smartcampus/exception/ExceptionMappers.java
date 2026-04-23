package com.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

// ── 409 Conflict: Room still has sensors ─────────────────────────────────────
@Provider
class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status",  409);
        body.put("error",   "Conflict");
        body.put("message", ex.getMessage());
        body.put("roomId",  ex.getRoomId());
        return Response.status(Response.Status.CONFLICT)
                       .type(MediaType.APPLICATION_JSON)
                       .entity(body)
                       .build();
    }
}

// ── 422 Unprocessable Entity: roomId reference doesn't exist ─────────────────
@Provider
class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status",  422);
        body.put("error",   "Unprocessable Entity");
        body.put("message", ex.getMessage());
        body.put("field",   ex.getField());
        body.put("value",   ex.getValue());
        return Response.status(422)
                       .type(MediaType.APPLICATION_JSON)
                       .entity(body)
                       .build();
    }
}

// ── 403 Forbidden: Sensor in MAINTENANCE cannot accept readings ───────────────
@Provider
class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status",   403);
        body.put("error",    "Forbidden");
        body.put("message",  ex.getMessage());
        body.put("sensorId", ex.getSensorId());
        body.put("sensorStatus", ex.getStatus());
        return Response.status(Response.Status.FORBIDDEN)
                       .type(MediaType.APPLICATION_JSON)
                       .entity(body)
                       .build();
    }
}

// ── 404 Not Found ─────────────────────────────────────────────────────────────
@Provider
class ResourceNotFoundExceptionMapper implements ExceptionMapper<ResourceNotFoundException> {

    @Override
    public Response toResponse(ResourceNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status",       404);
        body.put("error",        "Not Found");
        body.put("message",      ex.getMessage());
        body.put("resourceType", ex.getResourceType());
        body.put("id",           ex.getId());
        return Response.status(Response.Status.NOT_FOUND)
                       .type(MediaType.APPLICATION_JSON)
                       .entity(body)
                       .build();
    }
}

// ── 500 Global Safety Net ─────────────────────────────────────────────────────
@Provider
class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable ex) {
        // Log internally but never expose stack trace to client
        LOGGER.log(Level.SEVERE, "Unexpected error", ex);

        Map<String, Object> body = new HashMap<>();
        body.put("status",  500);
        body.put("error",   "Internal Server Error");
        body.put("message", "An unexpected error occurred. Please contact the API administrator.");
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                       .type(MediaType.APPLICATION_JSON)
                       .entity(body)
                       .build();
    }
}
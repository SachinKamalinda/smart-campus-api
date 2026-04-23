package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Part 3 – Sensor Operations & Part 4 sub-resource locator.
 * Manages /api/v1/sensors
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    // GET /api/v1/sensors[?type=CO2] – list all sensors, optionally filtered
    @GET
    public Response getSensors(@QueryParam("type") String type) {
        List<Sensor> result = store.getSensors().values().stream()
                .filter(s -> type == null || s.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
        return Response.ok(result).build();
    }

    // POST /api/v1/sensors – register a new sensor
    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor == null || sensor.getId() == null || sensor.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity(errorBody(400, "Bad Request", "Sensor ID is required."))
                           .build();
        }
        // Validate that the referenced room exists
        if (sensor.getRoomId() == null || store.getRoom(sensor.getRoomId()) == null) {
            throw new LinkedResourceNotFoundException("roomId", sensor.getRoomId());
        }
        // Duplicate check
        if (store.getSensor(sensor.getId()) != null) {
            return Response.status(Response.Status.CONFLICT)
                           .entity(errorBody(409, "Conflict", "Sensor ID already exists."))
                           .build();
        }
        // Default status if not provided
        if (sensor.getStatus() == null || sensor.getStatus().isBlank()) {
            sensor.setStatus("ACTIVE");
        }
        store.putSensor(sensor);

        // Update the room's sensorIds list
        Room room = store.getRoom(sensor.getRoomId());
        if (!room.getSensorIds().contains(sensor.getId())) {
            room.getSensorIds().add(sensor.getId());
        }

        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    // GET /api/v1/sensors/{sensorId}
    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null) throw new ResourceNotFoundException("Sensor", sensorId);
        return Response.ok(sensor).build();
    }

    // DELETE /api/v1/sensors/{sensorId}
    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null) throw new ResourceNotFoundException("Sensor", sensorId);

        // Remove sensor from its room's list
        Room room = store.getRoom(sensor.getRoomId());
        if (room != null) {
            room.getSensorIds().remove(sensorId);
        }
        store.deleteSensor(sensorId);
        return Response.noContent().build();
    }

    /**
     * Part 4 – Sub-Resource Locator Pattern.
     * Delegates /api/v1/sensors/{sensorId}/readings to SensorReadingResource.
     * This keeps each resource class focused and manageable.
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingsResource(@PathParam("sensorId") String sensorId) {
        // Validate sensor exists before handing off
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null) throw new ResourceNotFoundException("Sensor", sensorId);
        return new SensorReadingResource(sensorId);
    }

    private java.util.Map<String, Object> errorBody(int status, String error, String message) {
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("status",  status);
        body.put("error",   error);
        body.put("message", message);
        return body;
    }
}
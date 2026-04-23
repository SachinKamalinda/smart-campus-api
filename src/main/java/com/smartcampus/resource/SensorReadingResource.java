package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Part 4 – Historical Data Management.
 * Sub-resource for /api/v1/sensors/{sensorId}/readings
 *
 * Instantiated by SensorResource's sub-resource locator, not directly
 * by JAX-RS, so it does not need a @Path annotation at the class level.
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String    sensorId;
    private final DataStore store = DataStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // GET /api/v1/sensors/{sensorId}/readings – historical readings
    @GET
    public Response getReadings() {
        List<SensorReading> history = store.getReadings(sensorId);
        return Response.ok(history).build();
    }

    // POST /api/v1/sensors/{sensorId}/readings – append a new reading
    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = store.getSensor(sensorId);

        // 403: sensor in MAINTENANCE or OFFLINE cannot accept readings
        if (!"ACTIVE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensorId, sensor.getStatus());
        }

        // Auto-populate id and timestamp if not supplied by client
        if (reading.getId() == null || reading.getId().isBlank()) {
            reading = new SensorReading(reading.getValue());
        }

        store.addReading(sensorId, reading);

        // Side effect: update parent sensor's currentValue for data consistency
        sensor.setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }

    // GET /api/v1/sensors/{sensorId}/readings/{readingId}
    @GET
    @Path("/{readingId}")
    public Response getReading(@PathParam("readingId") String readingId) {
        return store.getReadings(sensorId).stream()
                .filter(r -> r.getId().equals(readingId))
                .findFirst()
                .map(r -> Response.ok(r).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                                .entity("{\"message\":\"Reading not found\"}")
                                .build());
    }
}
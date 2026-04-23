package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Part 1 – Discovery Endpoint.
 * GET /api/v1 returns API metadata and hypermedia links (HATEOAS).
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response discover() {
        Map<String, Object> response = new HashMap<>();
        response.put("name",        "Smart Campus Sensor & Room Management API");
        response.put("version",     "1.0");
        response.put("description", "RESTful API for managing campus rooms and IoT sensors.");
        response.put("contact",     "admin@smartcampus.ac.uk");

        // HATEOAS – hypermedia links guide clients to available resources
        Map<String, String> links = new HashMap<>();
        links.put("self",    "/smart-campus-api/api/v1/");
        links.put("rooms",   "/smart-campus-api/api/v1/rooms");
        links.put("sensors", "/smart-campus-api/api/v1/sensors");
        response.put("_links", links);

        return Response.ok(response).build();
    }
}
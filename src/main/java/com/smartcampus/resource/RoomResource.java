package com.smartcampus.resource;

import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Part 2 – Room Management.
 * Manages /api/v1/rooms
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore store = DataStore.getInstance();

    // GET /api/v1/rooms – list all rooms
    @GET
    public Response getAllRooms() {
        List<Room> rooms = new ArrayList<>(store.getRooms().values());
        return Response.ok(rooms).build();
    }

    // POST /api/v1/rooms – create a new room
    @POST
    public Response createRoom(Room room) {
        if (room == null || room.getId() == null || room.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity(errorBody(400, "Bad Request", "Room ID is required."))
                           .build();
        }
        if (store.getRoom(room.getId()) != null) {
            return Response.status(Response.Status.CONFLICT)
                           .entity(errorBody(409, "Conflict", "Room with ID '" + room.getId() + "' already exists."))
                           .build();
        }
        if (room.getSensorIds() == null) {
            room.setSensorIds(new ArrayList<>());
        }
        store.putRoom(room);
        return Response.status(Response.Status.CREATED).entity(room).build();
    }

    // GET /api/v1/rooms/{roomId} – fetch a specific room
    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRoom(roomId);
        if (room == null) throw new ResourceNotFoundException("Room", roomId);
        return Response.ok(room).build();
    }

    private Map<String, Object> errorBody(int status, String error, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("status",  status);
        body.put("error",   error);
        body.put("message", message);
        return body;
    }

    // DELETE /api/v1/rooms/{roomId} – delete with safety check
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRoom(roomId);
        if (room == null) {
            // Idempotent: second delete returns 404, first returns 204
            throw new ResourceNotFoundException("Room", roomId);
        }
        // Business rule: cannot delete a room that still has sensors
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(roomId);
        }
        store.deleteRoom(roomId);
        return Response.noContent().build(); // 204
    }
}
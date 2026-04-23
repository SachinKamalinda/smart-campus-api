package com.smartcampus.exception;

/** Thrown when a Room deletion is attempted but the room still contains sensors. */
public class RoomNotEmptyException extends RuntimeException {
    private final String roomId;

    public RoomNotEmptyException(String roomId) {
        super("Room " + roomId + " still has sensors assigned and cannot be deleted.");
        this.roomId = roomId;
    }

    public String getRoomId() { return roomId; }
}
package com.smartcampus.exception;

/** Thrown when a reading is posted to a sensor that is not ACTIVE (e.g. MAINTENANCE). */
public class SensorUnavailableException extends RuntimeException {
    private final String sensorId;
    private final String status;

    public SensorUnavailableException(String sensorId, String status) {
        super("Sensor " + sensorId + " is " + status + " and cannot accept new readings.");
        this.sensorId = sensorId;
        this.status   = status;
    }

    public String getSensorId() { return sensorId; }
    public String getStatus()   { return status; }
}
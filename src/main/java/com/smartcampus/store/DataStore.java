package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton in-memory data store.
 *
 * Because JAX-RS resource classes are instantiated per-request by default,
 * all shared state must live outside of them. This singleton, backed by
 * ConcurrentHashMap, provides thread-safe access to rooms, sensors, and
 * sensor readings without requiring a database.
 */
public class DataStore {

    private static final DataStore INSTANCE = new DataStore();

    // ConcurrentHashMap provides thread-safe read/write for concurrent requests
    private final Map<String, Room>              rooms        = new ConcurrentHashMap<>();
    private final Map<String, Sensor>            sensors      = new ConcurrentHashMap<>();
    private final Map<String, List<SensorReading>> readings   = new ConcurrentHashMap<>();

    private DataStore() {
        seedData();
    }

    public static DataStore getInstance() {
        return INSTANCE;
    }

    // ── Rooms ──────────────────────────────────────────────────────────────────

    public Map<String, Room> getRooms() {
        return rooms;
    }

    public Room getRoom(String id) {
        return rooms.get(id);
    }

    public void putRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    public boolean deleteRoom(String id) {
        return rooms.remove(id) != null;
    }

    // ── Sensors ────────────────────────────────────────────────────────────────

    public Map<String, Sensor> getSensors() {
        return sensors;
    }

    public Sensor getSensor(String id) {
        return sensors.get(id);
    }

    public void putSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
    }

    public boolean deleteSensor(String id) {
        return sensors.remove(id) != null;
    }

    // ── Readings ───────────────────────────────────────────────────────────────

    public List<SensorReading> getReadings(String sensorId) {
        return readings.getOrDefault(sensorId, new ArrayList<>());
    }

    public synchronized void addReading(String sensorId, SensorReading reading) {
        readings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
    }

    // ── Seed Data ──────────────────────────────────────────────────────────────

    private void seedData() {
        // Seed rooms
        Room r1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room r2 = new Room("LAB-101", "Computer Lab", 30);
        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);

        // Seed sensors
        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.5, "LIB-301");
        Sensor s2 = new Sensor("CO2-002",  "CO2",         "ACTIVE", 412.0, "LIB-301");
        Sensor s3 = new Sensor("OCC-003",  "Occupancy",   "MAINTENANCE", 0.0, "LAB-101");

        sensors.put(s1.getId(), s1);
        sensors.put(s2.getId(), s2);
        sensors.put(s3.getId(), s3);

        r1.getSensorIds().add(s1.getId());
        r1.getSensorIds().add(s2.getId());
        r2.getSensorIds().add(s3.getId());
    }
}
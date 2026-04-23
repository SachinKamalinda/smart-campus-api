# Smart Campus — Sensor & Room Management API
 
> **Title:** Smart Campus Sensor & Room Management API  
> **Technology:** JAX-RS + Apache Tomcat  
> **Deployment:** WAR file deployed on Apache Tomcat  

---

## Table of Contents

1. [API Overview](#api-overview)
2. [Project Structure](#project-structure)
3. [How to Build & Run](#how-to-build--run)
4. [API Endpoints Reference](#api-endpoints-reference)
5. [Sample curl Commands](#sample-curl-commands)
6. [Report — Question Answers](#report--question-answers)
   - [Part 1: Service Architecture & Setup](#part-1-service-architecture--setup)
   - [Part 2: Room Management](#part-2-room-management)
   - [Part 3: Sensor Operations & Linking](#part-3-sensor-operations--linking)
   - [Part 4: Deep Nesting with Sub-Resources](#part-4-deep-nesting-with-sub-resources)
   - [Part 5: Advanced Error Handling & Logging](#part-5-advanced-error-handling-exception-mapping--logging)

---

## API Overview

The Smart Campus API is a fully RESTful web service built using JAX-RS (Jersey implementation) deployed as a WAR file on Apache Tomcat. It provides a comprehensive interface for managing university campus Rooms and IoT Sensors, including a historical log of sensor readings.

**Base URL:** `http://localhost:8080/smart-campus-api/api/v1`

**Core Resources:**

| Resource | Path | Description |
|---|---|---|
| Discovery | `GET /` | API metadata and hypermedia links |
| Rooms | `/rooms` | Manage campus rooms |
| Sensors | `/sensors` | Manage IoT sensors |
| Readings | `/sensors/{id}/readings` | Historical sensor readings |

**Data is stored entirely in-memory using `ConcurrentHashMap` and `ArrayList`. No database is used.**

---

## Project Structure

```
smart-campus-api/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── smartcampus/
        │           ├── SmartCampusApplication.java
        │           ├── model/
        │           │   ├── Room.java
        │           │   ├── Sensor.java
        │           │   └── SensorReading.java
        │           ├── store/
        │           │   └── DataStore.java
        │           ├── resource/
        │           │   ├── DiscoveryResource.java
        │           │   ├── RoomResource.java
        │           │   ├── SensorResource.java
        │           │   └── SensorReadingResource.java
        │           ├── exception/
        │           │   ├── ResourceNotFoundException.java
        │           │   ├── RoomNotEmptyException.java
        │           │   ├── LinkedResourceNotFoundException.java
        │           │   ├── SensorUnavailableException.java
        │           │   └── ExceptionMappers.java
        │           └── filter/
        │               └── LoggingFilter.java
        └── webapp/
            └── WEB-INF/
                └── web.xml
```

---

## How to Build & Run

### Prerequisites

- JDK 11 or higher
- Apache Maven 3.6+
- Apache Tomcat 9 or 10
- NetBeans IDE 17+ (recommended)

### Step 1: Clone the Repository

```bash
git clone https://github.com/SachinKamalinda/smart-campus-api.git
cd smart-campus-api
```

### Step 2: Build the Project

```bash
mvn clean install
```

This compiles all sources and produces a WAR file at:
```
target/smart-campus-api.war
```

### Step 3: Deploy to Tomcat

**Option A: Via NetBeans (recommended)**
1. Open the project in NetBeans
2. Right-click project → **Properties → Run**
3. Make sure Apache Tomcat is selected as the server
4. Press **F6**. NetBeans will automatically deploy the WAR and open Tomcat

**Option B: Manual Tomcat deployment**
1. Copy `target/smart-campus-api.war` to your Tomcat `webapps/` folder
2. Start Tomcat:
```bash
# Windows
cd C:\apache-tomcat\bin
startup.bat

# Mac/Linux
cd /opt/apache-tomcat/bin
./startup.sh
```

### Step 4: Verify

Open your browser and navigate to:
```
http://localhost:8080/smart-campus-api/api/v1/
```

You should receive:
```json
{
  "name": "Smart Campus Sensor & Room Management API",
  "version": "1.0",
  "contact": "admin@smartcampus.ac.uk",
  "_links": {
    "self": "/smart-campus-api/api/v1/",
    "rooms": "/smart-campus-api/api/v1/rooms",
    "sensors": "/smart-campus-api/api/v1/sensors"
  }
}
```

### Step 5: Stop the Server

In NetBeans, click the **red square** button in the Output panel, or stop Tomcat via:
```bash
shutdown.bat   # Windows
./shutdown.sh  # Mac/Linux
```

---

## API Endpoints Reference

### Discovery

| Method | Path | Description | Response |
|---|---|---|---|
| GET | `/api/v1/` | API metadata + HATEOAS links | 200 OK |

### Rooms

| Method | Path | Description | Response |
|---|---|---|---|
| GET | `/api/v1/rooms` | List all rooms | 200 OK |
| POST | `/api/v1/rooms` | Create a new room | 201 Created |
| GET | `/api/v1/rooms/{roomId}` | Get room by ID | 200 / 404 |
| DELETE | `/api/v1/rooms/{roomId}` | Delete room (blocked if sensors present) | 204 / 409 |

### Sensors

| Method | Path | Description | Response |
|---|---|---|---|
| GET | `/api/v1/sensors` | List all sensors | 200 OK |
| GET | `/api/v1/sensors?type=CO2` | Filter sensors by type | 200 OK |
| POST | `/api/v1/sensors` | Register new sensor (validates roomId) | 201 / 422 |
| GET | `/api/v1/sensors/{sensorId}` | Get sensor by ID | 200 / 404 |
| DELETE | `/api/v1/sensors/{sensorId}` | Delete sensor | 204 / 404 |

### Sensor Readings (Sub-Resource)

| Method | Path | Description | Response |
|---|---|---|---|
| GET | `/api/v1/sensors/{sensorId}/readings` | Get all readings for sensor | 200 OK |
| POST | `/api/v1/sensors/{sensorId}/readings` | Add new reading (blocked if MAINTENANCE) | 201 / 403 |
| GET | `/api/v1/sensors/{sensorId}/readings/{readingId}` | Get specific reading | 200 / 404 |

### Error Responses

| HTTP Code | Scenario |
|---|---|
| 400 Bad Request | Missing required fields |
| 403 Forbidden | Posting reading to MAINTENANCE/OFFLINE sensor |
| 404 Not Found | Resource ID does not exist |
| 409 Conflict | Deleting a room that still has sensors |
| 422 Unprocessable Entity | Sensor POST with non-existent roomId |
| 500 Internal Server Error | Unexpected runtime error |

---

## Sample curl Commands

> All URLs use the Tomcat context path `/smart-campus-api`.

### 1. Get API Discovery (HATEOAS)
```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/
```

### 2. List All Rooms
```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/rooms
```

### 3. Create a New Room
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d "{\"id\": \"ENG-201\", \"name\": \"Engineering Lab\", \"capacity\": 40}"
```

### 4. Get a Specific Room
```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/rooms/LIB-301
```

### 5. Attempt to Delete a Room That Has Sensors (409 Conflict)
```bash
curl -X DELETE http://localhost:8080/smart-campus-api/api/v1/rooms/LIB-301
```

### 6. List All Sensors
```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/sensors
```

### 7. Filter Sensors by Type
```bash
curl -X GET "http://localhost:8080/smart-campus-api/api/v1/sensors?type=CO2"
```

### 8. Register a New Sensor
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\": \"TEMP-005\", \"type\": \"Temperature\", \"status\": \"ACTIVE\", \"currentValue\": 20.0, \"roomId\": \"LIB-301\"}"
```

### 9. Attempt to Register Sensor with Invalid roomId (422 Error)
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\": \"TEMP-999\", \"type\": \"Temperature\", \"status\": \"ACTIVE\", \"currentValue\": 0.0, \"roomId\": \"INVALID-ROOM\"}"
```

### 10. Post a New Sensor Reading
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d "{\"value\": 24.7}"
```

### 11. Get All Readings for a Sensor
```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings
```

### 12. Attempt to Post Reading to MAINTENANCE Sensor (403 Error)
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/OCC-003/readings \
  -H "Content-Type: application/json" \
  -d "{\"value\": 5.0}"
```

### 13. Delete a Sensor
```bash
curl -X DELETE http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-005
```

### 14. Delete a Room With No Sensors
```bash
curl -X DELETE http://localhost:8080/smart-campus-api/api/v1/rooms/ENG-201
```

---

## Report: Question Answers

---

### Part 1: Service Architecture & Setup

#### Q1.1: Default Lifecycle of a JAX-RS Resource Class

By default, JAX-RS creates a **new instance of each resource class for every incoming HTTP request**. This is known as the **per-request lifecycle**. Every time a client sends a request to, for example, `/api/v1/rooms`, JAX-RS instantiates a fresh `RoomResource` object to handle that request, processes it, and then discards the instance.

This architectural decision has significant implications for managing in-memory data. Because each resource instance is created and destroyed per request, **any instance level fields (like a `HashMap` stored inside `RoomResource`) would be reset with every request**, causing all previously created data to be permanently lost.

To prevent this, all shared mutable state must be stored **outside** of resource classes in a component that survives across requests. In this project, this is achieved through the `DataStore` singleton class. `DataStore` is initialised once using the Singleton pattern (`private static final DataStore INSTANCE = new DataStore()`) and is accessed via `DataStore.getInstance()` from within each resource class. Because the singleton lives for the entire JVM lifetime, data persists across all requests.

Additionally, since multiple requests can arrive concurrently, the `DataStore` uses `ConcurrentHashMap` instead of a plain `HashMap`. `ConcurrentHashMap` is thread-safe and allows concurrent reads and writes without explicit `synchronized` blocks, preventing race conditions such as two threads simultaneously modifying the same room or sensor collection and corrupting the data.

---

#### Q1.2: Why HATEOAS is a Hallmark of Advanced RESTful Design

**HATEOAS** (Hypermedia As The Engine Of Application State) is the principle that API responses should include **links to related actions and resources**, allowing clients to navigate the API dynamically rather than relying on hardcoded URLs.

In this API, the Discovery endpoint (`GET /api/v1/`) returns a `_links` object containing URLs for rooms and sensors. This means a client does not need to know the URL structure in advance. It can discover all available resources from the initial response alone.

**Benefits over static documentation:**

1. **Self-documenting API**: Clients can explore the API programmatically without reading external documentation. The response itself tells the client what it can do next.

2. **Decoupled clients**: If a URL path changes (e.g., `/rooms` moves to `/campus-rooms`), clients that follow links from responses are unaffected. Only the server-side link values need updating.

3. **Reduced errors**: Developers cannot accidentally hardcode a wrong URL because they always follow the links provided in the response.

4. **Guided workflows**: Responses can include contextual links. For example, a sensor response could include a link to its readings (`/sensors/TEMP-001/readings`), naturally guiding developers to the next logical step.

This makes HATEOAS especially powerful in large APIs where the resource hierarchy is complex, such as the Smart Campus system.

---

### Part 2: Room Management

#### Q2.1: Returning Only IDs vs Full Room Objects in a List

When designing `GET /api/v1/rooms`, there is a design choice between returning a list of full room objects or a list of IDs only. Each approach has meaningful trade-offs:

**Returning full objects (chosen approach):**
- The client receives all data in a single request, reducing the number of round-trips to the server.
- Ideal when clients typically need all room details immediately (e.g., displaying a dashboard).
- Increases response payload size, which can be costly when there are thousands of rooms.

**Returning IDs only:**
- The initial response is very lightweight and fast.
- However, for each room the client wishes to inspect, it must make an additional `GET /rooms/{id}` request. With 500 rooms, this could mean 500 additional HTTP requests. A pattern known as the **N+1 problem**.
- This approach is only preferable when clients rarely need full details, or when lazy-loading is acceptable.

**Conclusion:** For this campus management system, returning full objects is the correct choice because facilities managers need comprehensive data immediately. For very large datasets, **pagination** (e.g., `?page=1&size=20`) combined with full objects would provide the best balance of completeness and performance.

---

#### Q2.2: Is DELETE Idempotent in This Implementation?

In HTTP specification, DELETE is defined as an **idempotent** operation. Sending the same DELETE request multiple times should produce the same server state as sending it once.

In this implementation, DELETE is **partially idempotent**:

- **First DELETE** on `/rooms/ENG-201`: The room exists, is removed from the `DataStore`, and the server returns `204 No Content`.
- **Second DELETE** on `/rooms/ENG-201`: The room no longer exists. The server throws `ResourceNotFoundException`, which is mapped to `404 Not Found`.

The **server state is identical** after both calls and the room is gone. However, the **HTTP response codes differ** (204 vs 404). Purists argue true idempotency means the response should also be identical (always 204). Pragmatists argue that returning 404 on a second delete is acceptable and more informative to the client.

This is a deliberate design choice. Returning 404 for the second call communicates clearly that the resource was not found, which helps clients detect logic errors such as deleting the same room twice accidentally. The business rule that **a room with active sensors cannot be deleted** (returning 409 Conflict) also takes precedence, as it prevents orphaned sensor records in the data store.

---

### Part 3: Sensor Operations & Linking

#### Q3.1: Consequences of Sending Wrong Content-Type with @Consumes

The `POST /api/v1/sensors` method is annotated with `@Consumes(MediaType.APPLICATION_JSON)`, which declares that this endpoint only accepts requests with a `Content-Type: application/json` header.

**If a client sends data with a different content type**, such as `text/plain` or `application/xml`, JAX-RS handles the mismatch as follows:

1. The JAX-RS runtime inspects the `Content-Type` header of the incoming request before the method is even invoked.
2. It compares this against the `@Consumes` annotation on the resource method.
3. If there is no matching method that accepts the provided content type, JAX-RS automatically returns **HTTP 415 Unsupported Media Type**.
4. The resource method body is **never executed**. No Java code in `createSensor()` runs at all.

This is a significant benefit of declarative content negotiation. The framework acts as a first line of defence, rejecting malformed requests before they reach business logic. Without `@Consumes`, a raw `text/plain` body would arrive as an unparseable string and likely cause a `NullPointerException` or `JsonParseException` inside the method.

---

#### Q3.2: @QueryParam vs Path Segment for Filtering

This API implements sensor filtering via `GET /api/v1/sensors?type=CO2` using `@QueryParam("type")`. An alternative design would embed the filter in the path: `GET /api/v1/sensors/type/CO2`.

**Why `@QueryParam` is superior for filtering:**

1. **Semantic correctness**: Path segments should identify a specific resource. `/sensors/CO2` implies CO2 is a distinct resource, not a filtered view of the sensors collection. Query parameters are semantically designated for filtering, sorting, and searching.

2. **Optionality**: `@QueryParam` is naturally optional. `GET /api/v1/sensors` (no parameter) returns all sensors, while `GET /api/v1/sensors?type=CO2` returns a subset. With path-based filtering, a separate route definition would be required for the unfiltered case.

3. **Multiple filters**: Query parameters compose naturally: `?type=CO2&status=ACTIVE`. Path-based filtering becomes complex and unreadable: `/sensors/type/CO2/status/ACTIVE`.

4. **REST conventions**: The REST architectural style and standards such as the Google API Design Guide explicitly recommend query parameters for collection filtering. Path parameters are reserved for identifying individual resources.

5. **Caching**: URL-based query parameters are handled correctly by HTTP caches and proxies, which understand that `?type=CO2` is a filtered view, not a unique resource.

---

### Part 4: Deep Nesting with Sub-Resources

#### Q4.1: Benefits of the Sub-Resource Locator Pattern

The Sub-Resource Locator pattern is implemented in `SensorResource` via the method:

```java
@Path("/{sensorId}/readings")
public SensorReadingResource getReadingsResource(@PathParam("sensorId") String sensorId) {
    return new SensorReadingResource(sensorId);
}
```

This delegates handling of `/sensors/{sensorId}/readings` to a dedicated `SensorReadingResource` class rather than defining all reading related methods inside `SensorResource`.

**Architectural benefits:**

1. **Separation of concerns**: Each class has a single responsibility. `SensorResource` manages sensor CRUD; `SensorReadingResource` manages historical data. Neither class needs to know the internal details of the other.

2. **Manageable class size**: In large APIs, defining every nested path in one controller class creates files with hundreds of methods, making maintenance extremely difficult. Sub-resource locators split this complexity across multiple focused classes.

3. **Reusability**: `SensorReadingResource` could theoretically be reused by other resource classes that also need reading history. The class is not tied to a single URL pattern.

4. **Testability**: Smaller, focused classes are easier to unit test. `SensorReadingResource` can be instantiated and tested in isolation without bootstrapping the entire `SensorResource`.

5. **Context passing**: The locator method receives the `sensorId` path parameter and passes it to the sub-resource constructor, giving `SensorReadingResource` full context about which sensor it is operating on without needing to re-extract it from the URL.

---

### Part 5: Advanced Error Handling, Exception Mapping & Logging

#### Q5.1: Why HTTP 422 is More Semantically Accurate than 404 for Missing References

When a client POSTs a new sensor with a `roomId` that does not exist in the system, the correct response is **422 Unprocessable Entity**, not 404 Not Found.

The key distinction is:

- **404 Not Found** means the **requested URL/resource** cannot be located on the server. It refers to the endpoint itself being unreachable.
- **422 Unprocessable Entity** means the server **understands the request and the URL is valid**, but the **content of the request body is semantically invalid**. It refers to the data inside the payload.

In the sensor POST scenario, `POST /api/v1/sensors` is a perfectly valid, reachable endpoint (not 404). The problem is that the `roomId` field inside the JSON body references a room that does not exist. The request was received and parsed correctly; it simply cannot be actioned because of a broken reference within the payload.

Using 404 here would mislead the client into thinking the `/sensors` endpoint itself was missing, which is incorrect. HTTP 422 communicates precisely: "your request arrived, I understood it, but the data inside contains an invalid reference." This semantic precision helps client developers immediately understand they need to fix the `roomId` value, not the URL.

---

#### Q5.2: Security Risks of Exposing Java Stack Traces

Exposing raw Java stack traces in API error responses poses serious cybersecurity risks:

1. **Technology fingerprinting**: Stack traces reveal the exact framework versions in use (e.g., `jersey-server-2.39.1`). Attackers can look up known CVEs (Common Vulnerabilities and Exposures) for those specific versions and craft targeted exploits.

2. **Internal architecture disclosure**: Package names (e.g., `com.smartcampus.store.DataStore`) expose the internal structure of the application. An attacker learns class names, package hierarchies, and architectural patterns, making it easier to identify attack vectors.

3. **File path leakage**: Stack traces include absolute file paths on the server, revealing the operating system, directory structure, and deployment configuration.

4. **Logic exposure**: The sequence of method calls in a stack trace reveals business logic flow, which can be used to identify injection points, bypass logic, or replay partial operations.

5. **Denial of service assistance**: If a stack trace reveals which input caused a `NullPointerException`, an attacker can craft inputs that repeatedly trigger the same crash path.

The Global Exception Mapper in this project (`ExceptionMapper<Throwable>`) addresses all of these risks. It logs the full stack trace internally via `java.util.logging.Logger` (visible only to server administrators) while returning only a generic `500 Internal Server Error` message to the client, giving away no internal details whatsoever.

---

#### Q5.3: Why Use JAX-RS Filters for Cross-Cutting Concerns Like Logging

Cross-cutting concerns are behaviours that apply across many parts of an application regardless of business logic. Logging, authentication, CORS headers, and compression are classic examples.

**Using JAX-RS Filters (chosen approach):**

The `LoggingFilter` class implements both `ContainerRequestFilter` and `ContainerResponseFilter`. By annotating it with `@Provider`, JAX-RS automatically applies it to **every single request and response** without any modification to individual resource classes.

**Advantages over manual `Logger.info()` in every method:**

1. **DRY principle**: With 10+ resource methods across 4 resource classes, manually adding logging to every method means 10+ places to maintain. A filter centralises this in one class.

2. **Consistency**: Manual logging relies on every developer remembering to add log statements. A filter guarantees uniform logging for all requests with zero risk of omission.

3. **Non-invasive**: Resource methods remain focused purely on business logic. Logging is applied transparently as infrastructure, following the principle of separation of concerns.

4. **Easy to extend**: Adding request IDs, timing metrics or authentication checks to the filter immediately applies those capabilities to the entire API without touching any resource code.

5. **Easy to disable**: Removing the `@Provider` annotation or unregistering the filter disables logging everywhere in one change, compared to hunting through dozens of methods to remove manual log statements.

This is why filters are the industry-standard approach for cross-cutting concerns in JAX-RS applications.

---

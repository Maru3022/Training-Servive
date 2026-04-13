# Training Service — Full API & Code Documentation

## Table of Contents

1. [Overview](#overview)
2. [Technology Stack](#technology-stack)
3. [Project Structure](#project-structure)
4. [Configuration](#configuration)
5. [REST API Endpoints](#rest-api-endpoints)
   - [POST /trainings/bulk-load](#post-trainingsbulk-load)
   - [POST /trainings](#post-trainings)
   - [GET /trainings/{id}](#get-trainingsid)
   - [PUT /trainings/{id}](#put-trainingsid)
   - [DELETE /trainings/{id}](#delete-trainingsid)
   - [PATCH /trainings/sets/{setId}](#patch-trainingssetssetid)
   - [DELETE /trainings/exercises/{exerciseId}](#delete-trainingsexercisesexerciseid)
6. [Data Transfer Objects (DTOs)](#data-transfer-objects-dtos)
   - [TrainingDTO](#trainingdto)
   - [SetDTO](#setdto)
7. [Entity Models](#entity-models)
   - [Training](#training)
   - [ExerciseSet](#exerciseset)
8. [Service Layer](#service-layer)
   - [TrainingService](#trainingservice)
   - [TrainingBulkLoader](#trainingbulkloader)
9. [Application Entry Point](#application-entry-point)
10. [Tests](#tests)
11. [Docker & Deployment](#docker--deployment)
12. [Monitoring & Metrics](#monitoring--metrics)

---

## Overview

**Training Service** is a Spring Boot 3.5.0 microservice designed for managing workout/trainings data. It provides RESTful endpoints for CRUD operations on training sessions and exercise sets, as well as a bulk-loading mechanism for performance testing or data seeding.

- **Base package:** `com.example.training_service`
- **Server port:** `8085`
- **Base API path:** `/trainings`

---

## Technology Stack

| Technology | Version / Details |
|---|---|
| Java | 17 |
| Spring Boot | 3.5.0 |
| Spring Web | REST API |
| Spring Data JPA | Database persistence |
| PostgreSQL | Primary database |
| Redis | Caching layer |
| Kafka | Message broker |
| Lombok | Boilerplate reduction (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`) |
| Spring Actuator + Prometheus | Health & metrics monitoring |
| Springdoc OpenAPI | Swagger UI documentation |
| JUnit 5 + MockMvc | Testing |

---

## Project Structure

```
src/main/java/com/example/training_service/
├── TrainingServiceApplication.java       — Spring Boot entry point
├── TrainingBulkLoader.java               — Bulk data loading component
├── controller/
│   └── TrainingController.java           — REST endpoints
├── service/
│   └── TrainingService.java              — Business logic layer
├── model/
│   ├── Training.java                     — JPA entity: trainings table
│   └── ExerciseSet.java                  — JPA entity: exercise_sets table
└── dto/
    ├── TrainingDTO.java                  — DTO for training data
    └── SetDTO.java                       — DTO for exercise set data

src/test/java/com/example/training_service/
├── TrainingServiceApplicationTests.java  — Context load test
└── service/
    └── TrainingControllerTest.java       — Controller unit/integration tests
```

---

## Configuration

### application.properties

Key configuration properties (refer to `src/main/resources/application.properties`):

| Property | Description |
|---|---|
| `server.port=8085` | HTTP port the service listens on |
| `spring.datasource.*` | PostgreSQL connection settings |
| `spring.data.redis.*` | Redis connection settings |
| `spring.kafka.*` | Kafka broker settings |
| `spring.jpa.*` | JPA/Hibernate configuration |
| `management.*` | Actuator & Prometheus metrics endpoints |

---

## REST API Endpoints

All endpoints are under the base path `/trainings`.

---

### POST /trainings/bulk-load

Triggers a bulk data loading operation. Useful for performance testing or seeding initial data.

**Controller Method:**
```java
@PostMapping("/bulk-load")
public ResponseEntity<String> triggerBulkLoad(@RequestParam int count, @RequestParam int batchSize)
```

| Aspect | Details |
|---|---|
| **HTTP Method** | `POST` |
| **URL** | `/trainings/bulk-load` |
| **Request Parameters** | `count` (int) — total number of records to create<br>`batchSize` (int) — size of each batch |
| **Request Body** | None |
| **Response Body** | `"Bulk load triggered"` (plain text string) |
| **HTTP Status** | `200 OK` |
| **Service Called** | `TrainingBulkLoader.runBulkLoad(count, batchSize)` |

**Example Request:**
```bash
curl -X POST "http://localhost:8085/trainings/bulk-load?count=1000&batchSize=100"
```

**Example Response:**
```
Bulk load triggered
```

**Notes:**
- Currently a placeholder implementation — prints to console.
- Both `count` and `batchSize` are required query parameters.

---

### POST /trainings

Creates a new training session asynchronously.

**Controller Method:**
```java
@PostMapping
public ResponseEntity<UUID> postTrainings(@RequestBody TrainingDTO trainingDTO)
```

| Aspect | Details |
|---|---|
| **HTTP Method** | `POST` |
| **URL** | `/trainings` |
| **Request Body** | `TrainingDTO` (JSON) — full training data object |
| **Response Body** | `UUID` — the generated ID of the created training |
| **HTTP Status** | `202 Accepted` |
| **Service Called** | `TrainingService.createdTrainingAsync(trainingDTO)` |

**Example Request:**
```json
{
  "training_date": "2026-04-11",
  "user_id": "550e8400-e29b-41d4-a716-446655440000",
  "training_name": "Upper Body Workout",
  "training_status": "completed",
  "sets": [
    {
      "exercise_id": "660e8400-e29b-41d4-a716-446655440000",
      "weight": 50,
      "reps": 10,
      "order_": 1
    }
  ]
}
```

**Example Response:**
```json
"550e8400-e29b-41d4-a716-446655440001"
```

**Notes:**
- The `id` field in the request body is ignored; a new UUID is generated.
- Returns `202 Accepted` indicating asynchronous processing.
- If the JSON body is malformed, returns `400 Bad Request`.

---

### GET /trainings/{id}

Retrieves a training session by its UUID.

**Controller Method:**
```java
@GetMapping("/{id}")
public ResponseEntity<Training> getTraining(@PathVariable UUID id)
```

| Aspect | Details |
|---|---|
| **HTTP Method** | `GET` |
| **URL** | `/trainings/{id}` |
| **Path Variable** | `id` (UUID) — the training identifier |
| **Request Body** | None |
| **Response Body** | `Training` entity (JSON) |
| **HTTP Status** | `200 OK` |
| **Service Called** | `TrainingService.getTraining(id)` |

**Example Request:**
```bash
curl http://localhost:8085/trainings/550e8400-e29b-41d4-a716-446655440001
```

**Example Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "training_name": "Upper Body Workout"
}
```

**Notes:**
- Currently returns a stub Training with only `id` populated.
- If the training is not found, behavior depends on service implementation (currently does not throw).

---

### PUT /trainings/{id}

Fully updates an existing training session.

**Controller Method:**
```java
@PutMapping("/{id}")
public ResponseEntity<Training> updateTraining(@PathVariable UUID id, @RequestBody TrainingDTO trainingDTO)
```

| Aspect | Details |
|---|---|
| **HTTP Method** | `PUT` |
| **URL** | `/trainings/{id}` |
| **Path Variable** | `id` (UUID) — the training identifier |
| **Request Body** | `TrainingDTO` (JSON) — complete updated training data |
| **Response Body** | `Training` entity (JSON) — the updated training |
| **HTTP Status** | `200 OK` |
| **Service Called** | `TrainingService.updateFullTraining(id, trainingDTO)` |

**Example Request:**
```bash
curl -X PUT http://localhost:8085/trainings/550e8400-e29b-41d4-a716-446655440001 \
  -H "Content-Type: application/json" \
  -d '{
    "training_name": "Updated Name",
    "training_date": "2026-04-12",
    "user_id": "550e8400-e29b-41d4-a716-446655440000",
    "training_status": "planned",
    "sets": []
  }'
```

**Example Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "training_name": "Updated Name"
}
```

**Notes:**
- This is a full replacement (PUT semantics).
- Currently only updates `training_name` in the stub implementation.

---

### DELETE /trainings/{id}

Deletes a training session by its UUID.

**Controller Method:**
```java
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteTraining(@PathVariable UUID id)
```

| Aspect | Details |
|---|---|
| **HTTP Method** | `DELETE` |
| **URL** | `/trainings/{id}` |
| **Path Variable** | `id` (UUID) — the training identifier |
| **Request Body** | None |
| **Response Body** | None |
| **HTTP Status** | `204 No Content` |
| **Service Called** | `TrainingService.deleteTraining(id)` |

**Example Request:**
```bash
curl -X DELETE http://localhost:8085/trainings/550e8400-e29b-41d4-a716-446655440001
```

**Notes:**
- Idempotent operation — deleting a non-existent resource still returns `204`.
- No response body is returned.

---

### PATCH /trainings/sets/{setId}

Partially updates an exercise set (performance data).

**Controller Method:**
```java
@PatchMapping("/sets/{setId}")
public ResponseEntity<ExerciseSet> patchSet(@PathVariable UUID setId, @RequestBody SetDTO setDTO)
```

| Aspect | Details |
|---|---|
| **HTTP Method** | `PATCH` |
| **URL** | `/trainings/sets/{setId}` |
| **Path Variable** | `setId` (UUID) — the exercise set identifier |
| **Request Body** | `SetDTO` (JSON) — fields to update |
| **Response Body** | `ExerciseSet` entity (JSON) — the updated set |
| **HTTP Status** | `200 OK` |
| **Service Called** | `TrainingService.patchSetPerformance(setId, setDTO)` |

**Example Request:**
```bash
curl -X PATCH http://localhost:8085/trainings/sets/660e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json" \
  -d '{
    "weight": 60,
    "reps": 12,
    "order_": 2
  }'
```

**Example Response:**
```json
{
  "id": "660e8400-e29b-41d4-a716-446655440000",
  "weight": 60
}
```

**Notes:**
- Currently only the `weight` field from SetDTO is applied to ExerciseSet.
- PATCH semantics — only provided fields are updated.

---

### DELETE /trainings/exercises/{exerciseId}

Deletes a specific exercise (not a full training).

**Controller Method:**
```java
@DeleteMapping("/exercises/{exerciseId}")
public ResponseEntity<Void> deleteExercise(@PathVariable UUID exerciseId)
```

| Aspect | Details |
|---|---|
| **HTTP Method** | `DELETE` |
| **URL** | `/trainings/exercises/{exerciseId}` |
| **Path Variable** | `exerciseId` (UUID) — the exercise identifier |
| **Request Body** | None |
| **Response Body** | None |
| **HTTP Status** | `204 No Content` |
| **Service Called** | `TrainingService.deleteSpecificTraining(exerciseId)` |

**Example Request:**
```bash
curl -X DELETE http://localhost:8085/trainings/exercises/660e8400-e29b-41d4-a716-446655440000
```

**Notes:**
- Targets individual exercises within a training.
- No response body is returned.

---

## Data Transfer Objects (DTOs)

DTOs are used for API request/response bodies. They decouple the external API contract from internal entity representations.

---

### TrainingDTO

Transfers training session data between client and server.

**Package:** `com.example.training_service.dto`

| Field | Type | Description |
|---|---|---|
| `id` | `UUID` | Unique identifier of the training. Assigned by the server. |
| `training_date` | `LocalDate` | Date of the training session (format: `YYYY-MM-DD`). |
| `user_id` | `UUID` | Reference to the user who owns this training. |
| `training_name` | `String` | Human-readable name of the training. |
| `training_status` | `String` | Status of the training (e.g., "planned", "completed", "cancelled"). |
| `sets` | `List<SetDTO>` | Ordered list of exercise sets belonging to the training. |

**Lombok Annotations:**
- `@Data` — generates getters, setters, `toString()`, `equals()`, `hashCode()`
- `@NoArgsConstructor` — no-args constructor (required by Jackson deserialization)
- `@AllArgsConstructor` — all-args constructor

**JSON Example:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "training_date": "2026-04-11",
  "user_id": "550e8400-e29b-41d4-a716-446655440000",
  "training_name": "Leg Day",
  "training_status": "planned",
  "sets": [
    {
      "id": "770e8400-e29b-41d4-a716-446655440000",
      "exercise_id": "880e8400-e29b-41d4-a716-446655440000",
      "weight": 80,
      "reps": 8,
      "order_": 1
    }
  ]
}
```

---

### SetDTO

Transfers individual exercise set data (weight, reps, order).

**Package:** `com.example.training_service.dto`

| Field | Type | Description |
|---|---|---|
| `id` | `UUID` | Unique identifier of the set. Assigned by the server. |
| `exercise_id` | `UUID` | Reference to the parent exercise. |
| `weight` | `Integer` | Weight used in the set (in kg or lbs, unit not specified). |
| `reps` | `Integer` | Number of repetitions performed. |
| `order_` | `Integer` | Display/sort order of this set within the exercise. |

**Lombok Annotations:**
- `@Data` — generates getters, setters, `toString()`, `equals()`, `hashCode()`
- `@NoArgsConstructor` — no-args constructor
- `@AllArgsConstructor` — all-args constructor

**JSON Example:**
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440000",
  "exercise_id": "880e8400-e29b-41d4-a716-446655440000",
  "weight": 80,
  "reps": 8,
  "order_": 1
}
```

---

## Entity Models

JPA entities represent database tables. They are mapped using Hibernate.

---

### Training

Maps to the `trainings` table in the database.

**Package:** `com.example.training_service.model`

| Field | Type | JPA Annotation | Description |
|---|---|---|---|
| `id` | `UUID` | `@Id`, `@GeneratedValue` | Primary key, auto-generated UUID. |
| `training_name` | `String` | (implicit `@Column`) | Name of the training session. |

**Table:** `trainings`

**Lombok Annotations:**
- `@Data` — getters, setters, `toString()`, `equals()`, `hashCode()`
- `@NoArgsConstructor` — no-args constructor (required by JPA)
- `@AllArgsConstructor` — all-args constructor

**SQL (expected table structure):**
```sql
CREATE TABLE trainings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    training_name VARCHAR(255)
);
```

**Notes:**
- Currently a minimal entity; expected to expand with more fields (date, user_id, status, relationships to sets).

---

### ExerciseSet

Maps to the `exercise_sets` table in the database.

**Package:** `com.example.training_service.model`

| Field | Type | JPA Annotation | Description |
|---|---|---|---|
| `id` | `UUID` | `@Id`, `@GeneratedValue` | Primary key, auto-generated UUID. |
| `weight` | `Integer` | (implicit `@Column`) | Weight for this set. |

**Table:** `exercise_sets`

**Lombok Annotations:**
- `@Data` — getters, setters, `toString()`, `equals()`, `hashCode()`
- `@NoArgsConstructor` — no-args constructor (required by JPA)
- `@AllArgsConstructor` — all-args constructor

**SQL (expected table structure):**
```sql
CREATE TABLE exercise_sets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    weight INTEGER
);
```

**Notes:**
- Currently minimal; expected to expand with `reps`, `order_`, and a foreign key to a parent exercise/training.

---

## Service Layer

The `@Service` and `@Component` classes contain business logic. Currently all methods are **placeholder/stub implementations** and do not interact with a database.

---

### TrainingService

**Package:** `com.example.training_service.service`  
**Annotation:** `@Service`

This is the core business logic component. Injected into `TrainingController`.

---

#### `UUID createdTrainingAsync(TrainingDTO trainingDTO)`

Creates a new training session asynchronously.

| Aspect | Details |
|---|---|
| **Parameter** | `trainingDTO` — the training data to create |
| **Returns** | `UUID` — the generated ID for the new training |
| **Current Behavior** | Generates and returns a random UUID. Does not persist data. |
| **Expected Behavior** | Should convert DTO to entity, save to database, return the new ID. |

---

#### `Training getTraining(UUID id)`

Retrieves a training by its ID.

| Aspect | Details |
|---|---|
| **Parameter** | `id` — the UUID of the training to retrieve |
| **Returns** | `Training` — the found training entity |
| **Current Behavior** | Creates a new `Training`, sets its ID to the provided `id`, returns it. |
| **Expected Behavior** | Should query the database by ID. Throw exception if not found. |

---

#### `Training updateFullTraining(UUID id, TrainingDTO trainingDTO)`

Fully updates an existing training.

| Aspect | Details |
|---|---|
| **Parameter** | `id` — the UUID of the training to update |
| **Parameter** | `trainingDTO` — the new training data |
| **Returns** | `Training` — the updated training entity |
| **Current Behavior** | Creates a new `Training`, sets its ID and `training_name` from DTO, returns it. |
| **Expected Behavior** | Should find existing entity, update all fields from DTO, save and return. |

---

#### `void deleteTraining(UUID id)`

Deletes a training by its ID.

| Aspect | Details |
|---|---|
| **Parameter** | `id` — the UUID of the training to delete |
| **Returns** | `void` |
| **Current Behavior** | Does nothing (empty method body). |
| **Expected Behavior** | Should delete the entity from the database by ID. |

---

#### `ExerciseSet patchSetPerformance(UUID setId, SetDTO setDTO)`

Partially updates an exercise set's performance data.

| Aspect | Details |
|---|---|
| **Parameter** | `setId` — the UUID of the set to update |
| **Parameter** | `setDTO` — the set data containing new values |
| **Returns** | `ExerciseSet` — the updated exercise set entity |
| **Current Behavior** | Creates a new `ExerciseSet`, sets its ID and `weight` from DTO, returns it. |
| **Expected Behavior** | Should find existing set, apply only provided fields (PATCH), save and return. |

---

#### `void deleteSpecificTraining(UUID exerciseId)`

Deletes a specific exercise.

| Aspect | Details |
|---|---|
| **Parameter** | `exerciseId` — the UUID of the exercise to delete |
| **Returns** | `void` |
| **Current Behavior** | Does nothing (empty method body). |
| **Expected Behavior** | Should delete the exercise entity from the database. |

---

### TrainingBulkLoader

**Package:** `com.example.training_service`  
**Annotation:** `@Component`

A component for bulk data operations, typically used for load testing or initial data seeding.

---

#### `void runBulkLoad(int count, int batchSize)`

Executes a bulk load operation.

| Aspect | Details |
|---|---|
| **Parameter** | `count` — total number of records to generate/insert |
| **Parameter** | `batchSize` — number of records per batch insert |
| **Returns** | `void` |
| **Current Behavior** | Prints `"Running bulk load: count=X, batchSize=Y"` to standard output. |
| **Expected Behavior** | Should generate and persist `count` training records in batches of `batchSize`. |

---

## Application Entry Point

### TrainingServiceApplication

**Package:** `com.example.training_service`  
**File:** `TrainingServiceApplication.java`

```java
@SpringBootApplication
public class TrainingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TrainingServiceApplication.class, args);
    }
}
```

| Aspect | Details |
|---|---|
| **Annotation** | `@SpringBootApplication` — enables auto-configuration, component scanning, and configuration properties |
| **main()** | Standard Spring Boot entry point; bootstraps the application context |
| **Component Scanning** | Scans `com.example.training_service` and all sub-packages |

---

## Tests

### TrainingServiceApplicationTests

**Package:** `com.example.training_service`  
**File:** `TrainingServiceApplicationTests.java`

| Aspect | Details |
|---|---|
| **Annotation** | `@SpringBootTest` — loads the full application context |
| **Test Method** | `contextLoads()` — verifies that the Spring context initializes without errors |
| **Type** | Smoke test |

---

### TrainingControllerTest

**Package:** `com.example.training_service.service`  
**File:** `TrainingControllerTest.java`

| Aspect | Details |
|---|---|
| **Annotation** | `@WebMvcTest(TrainingController.class)` — loads only the web layer for fast tests |
| **Mocked Beans** | `@MockBean TrainingBulkLoader`, `@MockBean TrainingService` |
| **Test Framework** | JUnit 5 + Spring MockMvc |

#### Test Methods:

| Test Method | What It Tests |
|---|---|
| `triggerBulkLoad_ShouldReturnOk` | `POST /trainings/bulk-load` returns `200 OK` |
| `postTrainings_ShouldReturnAccepted` | `POST /trainings` returns `202 Accepted` with a UUID |
| `postTrainings_ShouldReturnBadRequest_WhenInvalidJson` | `POST /trainings` with invalid JSON returns `400 Bad Request` |
| `getTraining_ShouldReturnTraining` | `GET /trainings/{id}` returns the training object |
| `updateTraining_ShouldReturnOk` | `PUT /trainings/{id}` returns `200 OK` with updated training |
| `deleteTrainings_ShouldReturnNoContent` | `DELETE /trainings/{id}` returns `204 No Content` |
| `patchSet_ShouldReturnOk` | `PATCH /trainings/sets/{setId}` returns `200 OK` with updated set |
| `deleteExercise_ShouldReturnNoContent` | `DELETE /trainings/exercises/{exerciseId}` returns `204 No Content` |

---

## Docker & Deployment

### Dockerfile

Multi-stage build:
1. **Build stage** — uses Maven to compile the project
2. **Runtime stage** — uses a lightweight JRE image to run the JAR

---

## Monitoring & Metrics

### Spring Boot Actuator

Exposed endpoints (configure via `management.endpoints.web.exposure.include`):
- `/actuator/health` — application health status
- `/actuator/info` — application information
- `/actuator/metrics` — various application metrics
- `/actuator/prometheus` — metrics in Prometheus format

### Prometheus

Configured via `prometheus.yml` to scrape metrics from the application. Prometheus collects:
- JVM metrics (memory, GC, threads)
- HTTP request metrics (latency, count, errors)
- Database connection pool metrics

### Grafana

Accessible at the configured Grafana URL. Pre-configured dashboards can visualize Prometheus data for monitoring service health and performance.

---

## Current State & Next Steps

> **⚠️ IMPORTANT:** All service methods are currently **stub/placeholder implementations**. The following components are **not yet implemented**:

| Missing Component | Description |
|---|---|
| **Repository layer** | No `@Repository` interfaces (e.g., `TrainingRepository`, `ExerciseSetRepository`) exist. No database operations are performed. |
| **DTO ↔ Entity mapping** | No mapper class exists to convert between DTOs and entities. |
| **Database persistence** | Service methods do not call any repositories; data is not persisted. |
| **Async processing** | `createdTrainingAsync` does not use `@Async` or any async executor. |
| **Validation** | No `@Valid` annotations or validation logic on request bodies. |
| **Error handling** | No `@ControllerAdvice` or `@ExceptionHandler` for centralized error handling. |
| **Kafka integration** | Kafka is in the dependencies but not wired into any code paths. |
| **Redis caching** | Redis is in the dependencies but not used in any caching logic. |
| **Security** | No Spring Security configuration exists. |
| **OpenAPI/Swagger** | springdoc is in dependencies but no additional configuration is present. |

---

## API Endpoints Summary

| Method | Endpoint | Description | Status Code |
|---|---|---|---|
| `POST` | `/trainings/bulk-load?count=&batchSize=` | Trigger bulk load | `200 OK` |
| `POST` | `/trainings` | Create a new training | `202 Accepted` |
| `GET` | `/trainings/{id}` | Get training by ID | `200 OK` |
| `PUT` | `/trainings/{id}` | Full update of a training | `200 OK` |
| `DELETE` | `/trainings/{id}` | Delete a training | `204 No Content` |
| `PATCH` | `/trainings/sets/{setId}` | Update exercise set | `200 OK` |
| `DELETE` | `/trainings/exercises/{exerciseId}` | Delete an exercise | `204 No Content` |

# Training Service API Documentation

## Overview

Training Service is a Spring Boot 3.5.0 backend that exposes REST endpoints for managing training sessions and exercise sets. The current codebase is a strong backend skeleton with presentation improvements, OpenAPI polish, and placeholder business logic ready to be expanded.

- Base package: `com.example.training_service`
- Default port: `8085`
- Base API path: `/trainings`

## Runtime Entry Points

- Landing page: `/`
- Swagger UI: `/swagger-ui/index.html`
- OpenAPI JSON: `/v3/api-docs`
- Health endpoint: `/actuator/health`
- Prometheus metrics: `/actuator/prometheus`

## Technology Stack

| Area | Details |
| :-- | :-- |
| Language | Java 17 |
| Framework | Spring Boot 3.5.0 |
| API | Spring Web + Spring Validation |
| Persistence | Spring Data JPA + PostgreSQL dependency present |
| Caching | Redis dependency present |
| Messaging | Kafka dependency present |
| Docs | Springdoc OpenAPI / Swagger UI |
| Observability | Spring Boot Actuator + Micrometer Prometheus |
| Tests | JUnit 5 + MockMvc |

## Project Structure

```text
src/main/java/com/example/training_service
|- TrainingServiceApplication.java
|- TrainingBulkLoader.java
|- config/OpenApiConfig.java
|- controller/TrainingController.java
|- dto/TrainingDTO.java
|- dto/SetDTO.java
|- model/Training.java
|- model/ExerciseSet.java
`- service/TrainingService.java

src/main/resources
|- application.properties
`- static/
   |- index.html
   `- styles.css
```

## API Endpoints

### POST `/trainings/bulk-load`

Triggers the placeholder bulk-loader.

Query params:
- `count` - total records requested
- `batchSize` - batch size per operation

Response:
- `200 OK`
- Body: `"Bulk load triggered"`

### POST `/trainings`

Creates a training asynchronously and returns a generated UUID.

Request example:

```json
{
  "training_date": "2026-04-17",
  "user_id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "training_name": "Strength Block A",
  "training_status": "PLANNED",
  "sets": [
    {
      "exercise_id": "550e8400-e29b-41d4-a716-446655440000",
      "weight": 80,
      "reps": 8,
      "order_": 1
    }
  ]
}
```

Response:
- `202 Accepted`
- Body: generated training UUID

### GET `/trainings/{id}`

Returns a training projection. In the current implementation this is a stub object with the requested UUID applied.

### PUT `/trainings/{id}`

Replaces a training. Current implementation updates and returns a stub object with the provided `training_name`.

### DELETE `/trainings/{id}`

Deletes a training resource.

Response:
- `204 No Content`

### PATCH `/trainings/sets/{setId}`

Partially updates a set. Current implementation returns a stub `ExerciseSet` with the provided weight.

### DELETE `/trainings/exercises/{exerciseId}`

Deletes a specific exercise resource.

Response:
- `204 No Content`

## DTO Contracts

### `TrainingDTO`

| Field | Type | Required | Notes |
| :-- | :-- | :-- | :-- |
| `id` | `UUID` | No | Optional for create |
| `training_date` | `LocalDate` | Yes | Session date |
| `user_id` | `UUID` | Yes | Training owner |
| `training_name` | `String` | Yes | Human-readable title |
| `training_status` | `String` | Yes | Current lifecycle state |
| `sets` | `List<SetDTO>` | Yes | Ordered collection of sets |

### `SetDTO`

| Field | Type | Required | Notes |
| :-- | :-- | :-- | :-- |
| `id` | `UUID` | No | Optional for create |
| `exercise_id` | `UUID` | Yes | Parent exercise id |
| `weight` | `Integer` | No | Must be `>= 0` when present |
| `reps` | `Integer` | No | Must be `>= 1` when present |
| `order_` | `Integer` | No | Must be `>= 1` when present |

## What Is Implemented vs Placeholder

Implemented today:
- Web layer and endpoint routing
- DTO validation
- OpenAPI metadata and examples
- Static landing page and service presentation
- Actuator and Swagger exposure
- Controller-level tests

Still placeholder:
- Repository layer
- Real database persistence
- Async execution infrastructure
- Kafka publishing/consuming flows
- Redis-backed caching logic
- Centralized exception handling

## Testing

Main test coverage currently focuses on the controller layer through `MockMvc`, including:

- training creation
- route status codes
- delete flows
- patch set flow
- malformed JSON handling

Run verification with:

```bash
./mvnw -B -ntp clean verify
```

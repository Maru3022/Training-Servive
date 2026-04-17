# Training Service

Training Service is a Spring Boot backend for managing workout sessions, exercise sets, and bulk-loading flows. This repository now includes a polished landing page at the root URL, improved OpenAPI presentation, and a cleaner project narrative for demos and reviews.

## Current Shape

- REST API for training CRUD and set patch operations
- Bulk-load trigger endpoint for stress or seed scenarios
- Swagger UI and raw OpenAPI docs out of the box
- Actuator and Prometheus-ready metrics exposure
- Visual landing page for faster onboarding into the service

## Stack

- Java 17
- Spring Boot 3.5.0
- Spring Web
- Spring Validation
- Spring Data JPA
- PostgreSQL
- Redis
- Kafka
- Springdoc OpenAPI / Swagger UI
- Micrometer + Prometheus
- JUnit 5 / MockMvc

## What Changed In This Refresh

- Added a custom home page at `/` so the service looks intentional instead of blank
- Improved Swagger/OpenAPI metadata, grouping, and request examples
- Added validation annotations to request DTOs
- Aligned documentation with the code that actually exists today
- Kept the current backend behavior stable while making the project far more presentable

## Routes

| Method | Endpoint | Purpose |
| :-- | :-- | :-- |
| `POST` | `/trainings/bulk-load` | Trigger placeholder bulk load |
| `POST` | `/trainings` | Create training asynchronously |
| `GET` | `/trainings/{id}` | Get training by id |
| `PUT` | `/trainings/{id}` | Replace training |
| `DELETE` | `/trainings/{id}` | Delete training |
| `PATCH` | `/trainings/sets/{setId}` | Patch set performance |
| `DELETE` | `/trainings/exercises/{exerciseId}` | Delete exercise |

## Useful URLs

- App home: `http://localhost:8085/`
- Swagger UI: `http://localhost:8085/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8085/v3/api-docs`
- Health: `http://localhost:8085/actuator/health`
- Prometheus: `http://localhost:8085/actuator/prometheus`

## Local Run

1. Start PostgreSQL, Redis, and Kafka with your preferred local setup.
2. Run the application:

```bash
./mvnw spring-boot:run
```

3. Open the landing page or Swagger UI in the browser.

## Build And Test

```bash
./mvnw -B -ntp clean verify
```

## Architecture Reality Check

The repository already contains production-oriented dependencies and CI/CD placeholders, but the service layer is still mostly stubbed. Today this project is best understood as:

- a clean API skeleton
- a presentation-ready backend demo
- a good foundation for later repository, async, caching, and messaging implementations

## Next Good Steps

- Add repositories and real persistence flows
- Introduce centralized exception handling
- Wire Kafka and Redis into live business paths
- Add integration tests with Testcontainers
- Introduce schema migrations with Flyway or Liquibase

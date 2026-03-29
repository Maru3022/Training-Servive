# Training Service

Production-ready backend for training plans with asynchronous processing, transactional outbox, caching, and observability.

## Core Features

- CRUD for trainings, exercises and sets
- Asynchronous event flow via Kafka
- Transactional Outbox pattern for reliable event delivery
- Redis caching for hot reads
- Bulk data load endpoint for stress scenarios
- Actuator metrics for Prometheus/Grafana

## Tech Stack

- Java 21
- Spring Boot 3.4.x
- Spring Web, Validation, Data JPA
- PostgreSQL
- Redis
- Apache Kafka
- OpenAPI / Swagger UI
- Docker / Docker Compose
- GitHub Actions CI/CD

## Project Layout

- `src/main/java` - application code
- `src/main/resources` - runtime config
- `src/test/java` - unit/web tests
- `src/k6` - load testing scripts
- `.github/workflows` - CI/CD pipeline

## API Endpoints

| Method | Endpoint | Purpose |
| :-- | :-- | :-- |
| `POST` | `/trainings` | Create training asynchronously |
| `GET` | `/trainings/{id}` | Get training by id |
| `PUT` | `/trainings/{id}` | Full training update |
| `DELETE` | `/trainings/{id}` | Delete training |
| `PATCH` | `/trainings/sets/{setId}` | Update set weight/reps |
| `DELETE` | `/trainings/exercises/{exerciseId}` | Delete exercise |
| `POST` | `/trainings/bulk-load?count=100000&batchSize=5000` | Bulk insert |

Swagger UI:

- `http://localhost:8085/swagger-ui/index.html`

## Local Run

1) Start dependencies:

```bash
docker compose up -d postgres redis zookeeper kafka
```

2) Start service:

```bash
./mvnw spring-boot:run
```

3) Health check:

```bash
curl http://localhost:8085/actuator/health
```

## Full Stack (App + Monitoring)

```bash
docker compose up -d
```

Useful URLs:

- App: `http://localhost:8085`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`

## Build and Test

```bash
./mvnw -B -ntp clean verify
```

## CI/CD

Workflow: `.github/workflows/main.yml`

Pipeline includes:

- Build & Test
- Code Quality
- Docker Build & Push
- Deploy to Staging
- Integration Tests
- Deploy to Production
- Notify

Image tags:

- `latest`
- `sha-<commit>`

## Configuration

Main runtime variables:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_KAFKA_BOOTSTRAP_SERVERS`
- `SPRING_DATA_REDIS_HOST`
- `SPRING_DATA_REDIS_PORT`

## Next Improvements

- Move all secrets to environment/GitHub Secrets
- Add Flyway/Liquibase migrations for schema changes
- Add Testcontainers integration tests for Kafka/PostgreSQL
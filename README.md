# 🏋️ Training Service

Core-сервис фитнес-платформы [FitFlow](https://github.com/Maru3022/project-hub) — управляет тренировками, упражнениями и пользовательскими «кабинетами», участвует в распределённой Saga-транзакции и отдаёт статистику по прогрессу пользователя.

[![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)](.)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-brightgreen?logo=spring)](.)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Flyway-336791?logo=postgresql)](.)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-Event%20Driven-231F20?logo=apachekafka)](.)
[![Redis](https://img.shields.io/badge/Redis-Cache-DC382D?logo=redis)](.)
[![K8s](https://img.shields.io/badge/Kubernetes-k6%20load--tested-326CE5?logo=kubernetes)](.)

---

## Что делает сервис

- **CRUD тренировок и подходов** — создание тренировки с набором упражнений (`sets`), фильтрация по пользователю/статусу/диапазону дат с пагинацией, частичное обновление веса/повторов/порядка подхода.
- **Каталог упражнений** — отдельный CRUD по группам мышц (`MuscleGroup`).
- **Пользователи и статистика** — история тренировок пользователя и агрегированная статистика (`/users/{id}/stats`): объём, частота, прогресс по упражнениям.
- **Участие в Saga "CREATE_PROGRAM"** — при регистрации пользователя слушает команду от [Saga Orchestrator](https://github.com/Maru3022/Saga-Orchestrator) и создаёт `TrainingCabinet`, с идемпотентной обработкой повторов и компенсацией (откатом) при ошибке в одном из последующих шагов саги.
- **Bulk-loader** — служебный эндпоинт для массовой генерации тренировок, используется как сид-данные перед нагрузочным тестированием.

## Архитектура взаимодействий

```
                         ┌──────────────────┐
                         │   API Gateway     │
                         └────────┬─────────┘
                                  │ /api/training/**
                                  ▼
                         ┌──────────────────┐        register      ┌──────────────┐
                         │  Training Service ├──────────────────────►   Eureka     │
                         └────────┬─────────┘                       └──────────────┘
                                  │
              ┌───────────────────┼────────────────────┐
              ▼                   ▼                     ▼
        PostgreSQL            Redis-кэш             Apache Kafka
     (trainings, sets,      (Training по id)     training.created
      exercises, users,                           training.status-changed
      training_cabinet)                           saga.cabinet.create / response / compensate
                                                            ▲
                                                            │
                                                  ┌──────────────────┐
                                                  │ Saga Orchestrator │
                                                  └──────────────────┘
```

## Saga: как сервис участвует в распределённой транзакции

Training Service — один из шагов саги **CREATE_PROGRAM**, которая запускается при регистрации нового пользователя.

1. Слушает `saga.cabinet.create` → создаёт `TrainingCabinet` для пользователя.
2. Идемпотентность: повторная доставка с тем же `correlationId` **не создаёт второй кабинет**, а просто переотправляет тот же успешный ответ — защита от дублей при at-least-once delivery в Kafka.
3. Публикует результат в `saga.cabinet.response` (`success: true/false`) — оркестратор продвигает сагу дальше или запускает компенсацию.
4. Если падает следующий шаг саги (например, расчёт питания) — оркестратор присылает `saga.cabinet.compensate`, сервис помечает кабинет как `DELETED`. Никаких ручных «исправлений в БД» — откат бизнес-транзакции описан в коде.

Кроме саги, сервис использует **транзакционный Outbox** (`UserService`, `OutboxEventRepository`) для исходящих доменных событий: запись в таблицу outbox происходит в той же транзакции, что и сохранение бизнес-сущности, а отдельный `OutboxProcessor` каждые 3 секунды вычитывает `PENDING`-события и публикует их в Kafka — что исключает рассинхрон между БД и шиной сообщений при падении процесса между двумя операциями.

## API

### Trainings (`/trainings`)

| Method | Endpoint | Описание |
|---|---|---|
| `POST` | `/trainings` | Создать тренировку (асинхронно, публикует `TrainingCreatedEvent`) |
| `GET` | `/trainings/{id}` | Получить тренировку по id (ответ кэшируется в Redis) |
| `GET` | `/trainings` | Список тренировок с фильтрами `userId`, `status`, `from`, `to` + пагинация |
| `PUT` | `/trainings/{id}` | Полная замена тренировки |
| `DELETE` | `/trainings/{id}` | Удалить тренировку |
| `PATCH` | `/trainings/sets/{setId}` | Частично обновить подход (вес/повторы/порядок) |
| `DELETE` | `/trainings/exercises/{exerciseId}` | Удалить подход из тренировки |
| `POST` | `/trainings/bulk-load?count=&batchSize=` | Запустить пакетную генерацию тренировок (для сидирования/нагрузочных тестов) |

### Exercises (`/exercises`)

| Method | Endpoint | Описание |
|---|---|---|
| `POST` | `/exercises` | Добавить упражнение в каталог |
| `GET` | `/exercises/{id}` | Получить упражнение по id |
| `GET` | `/exercises` | Список упражнений (фильтр по группе мышц, пагинация) |
| `PUT` | `/exercises/{id}` | Обновить упражнение |
| `DELETE` | `/exercises/{id}` | Удалить упражнение |

### Users (`/users`)

| Method | Endpoint | Описание |
|---|---|---|
| `POST` | `/users` | Создать пользователя |
| `GET` | `/users/{id}` | Получить пользователя |
| `GET` | `/users` | Список пользователей с пагинацией |
| `PUT` | `/users/{id}` | Обновить пользователя |
| `DELETE` | `/users/{id}` | Мягкое удаление |
| `DELETE` | `/users/{id}/hard` | Полное (hard) удаление |
| `GET` | `/users/{id}/trainings` | Тренировки пользователя |
| `GET` | `/users/{id}/stats` | Агрегированная статистика по тренировкам |

### Технические эндпоинты

| Endpoint | Описание |
|---|---|
| `/swagger-ui/index.html` | Интерактивная документация API (OpenAPI 3) |
| `/v3/api-docs` | OpenAPI-спецификация в JSON |
| `/actuator/health` | Health-check |
| `/actuator/prometheus` | Метрики для Prometheus |

## Технологический стек

| Категория | Технологии |
|---|---|
| Язык / Framework | Java 17, Spring Boot 3.5.0 |
| Web / API | Spring Web, Spring Validation, Springdoc OpenAPI / Swagger UI |
| Данные | Spring Data JPA, PostgreSQL, Flyway (миграции) |
| Кэш | Redis (`@Cacheable` / `@CacheEvict` на чтении тренировки) |
| Messaging | Spring Kafka (доменные события + Saga-команды), Outbox Pattern |
| Service Discovery | Netflix Eureka Client |
| Observability | Spring Boot Actuator, Micrometer + Prometheus |
| Тестирование | JUnit 5, MockMvc, k6 (нагрузочное тестирование) |
| Контейнеризация | Docker, Kubernetes-манифесты (`namespace`, `postgres`, `redis`, `kafka`, `app`, `monitoring`) |

## Нагрузочное тестирование

В `k8s/k6/script.js` описан полноценный профиль нагрузки (ramp-up → steady → spike → ramp-down: 10 → 30 → 50 → 80 → 50 → 0 VUs) с заданными SLA-порогами:

- 95% запросов быстрее **1.5 секунды** (`p(95) < 1500ms`);
- доля ошибок **< 5%**;
- успешность проверок (`checks`) **> 99%**.

Тест разворачивается прямо в Kubernetes как `Job` (`k8s/k6/job.yaml`), а результаты визуализируются в Grafana-дашборде (`k8s/monitoring/grafana/configmap-dashboard-k6.yaml`) рядом с метриками самого сервиса — то есть нагрузочное тестирование интегрировано в инфраструктуру, а не запускается «руками с ноутбука».

## Локальный запуск

### Предварительно нужны
- Java 17, Maven 3.9+ (или `./mvnw`)
- PostgreSQL, Redis, Kafka (локально или через Docker)
- Eureka Server, если нужна регистрация в Service Discovery

### Запуск

```bash
# поднять зависимости (пример — под свой docker-compose)
docker run -d -p 5432:5432 -e POSTGRES_DB=training_db -e POSTGRES_USER=myuser -e POSTGRES_PASSWORD=secret postgres:16
docker run -d -p 6379:6379 redis:7

# собрать и запустить сервис
./mvnw clean package -DskipTests
java -jar target/Training_Service-0.0.1-SNAPSHOT.jar
```

По умолчанию сервис поднимается на порту **8085**.

| Что | Где |
|---|---|
| Главная страница | `http://localhost:8085/` |
| Swagger UI | `http://localhost:8085/swagger-ui/index.html` |
| Health | `http://localhost:8085/actuator/health` |
| Prometheus | `http://localhost:8085/actuator/prometheus` |

### Развёртывание в Kubernetes

Полный сценарий — Postgres, Redis, Kafka, само приложение, Prometheus и Grafana с готовыми дашбордами — описан в [`K8S-GUIDE.md`](K8S-GUIDE.md), включая запуск k6 как `Job` внутри кластера.

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/postgres/
kubectl apply -f k8s/redis/
kubectl apply -f k8s/kafka/
kubectl apply -f k8s/app/
kubectl apply -f k8s/monitoring/
```

## Связанные репозитории

Часть микросервисной платформы [FitFlow](https://github.com/Maru3022/project-hub):

- [API Gateway](https://github.com/Maru3022/API_Gateway)
- [Eureka Server](https://github.com/Maru3022/Eureka-server)
- [Saga Orchestrator](https://github.com/Maru3022/Saga-Orchestrator)
- [Trains Service](https://github.com/Maru3022/Trains-Service)
- [Nutrition Service](https://github.com/Maru3022/Training-Nutrition)
- [Notification Service](https://github.com/Maru3022/Training_Notification)
- [Recommendation Service](https://github.com/Maru3022/Recommendation-Service)

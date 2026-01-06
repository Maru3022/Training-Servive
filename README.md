# 🏋️ Training Service (Producer)

**Service Type:** Main Data Source / System of Record  
**Domain:** Workout & Exercise Management

This microservice acts as the single source of truth for the system's training data. It manages the lifecycle of training sessions (CRUD) and asynchronously propagates state changes to downstream services (Analytics, Gamification, Notifications) via Apache Kafka.

To ensure data consistency between the database and the message broker, this service implements the **Transactional Outbox Pattern**.

---

## 🚀 Key Features

* **Training CRUD:** Full management of training sessions (Create, Read, Update, Delete).
* **Deep Tracking:** Granular recording of exercises, sets, weights, repetitions, and execution order.
* **Event Sourcing:** Implements the Publisher pattern. Every meaningful state change broadcasts an event to Kafka.
* **Reliability:** Guarantees "At-Least-Once" delivery of events using the Transactional Outbox pattern.

---

## 🛠 Tech Stack

| Component | Technology | Role |
| :--- | :--- | :--- |
| **Language** | Go / Java / Python | *Service Implementation* |
| **Database** | **PostgreSQL** | *Primary persistent storage* |
| **Message Broker** | **Apache Kafka** | *Asynchronous event bus* |
| **Architecture** | **REST + Async Messaging** | *Hybrid communication* |
| **Pattern** | **Transactional Outbox** | *Distributed data consistency* |

---

## 🏗 Architecture & Design: The "Dual Write" Solution

As the **Main Data Source**, this service faces the atomicity challenge: writing to the Database and publishing to Kafka must both succeed, or both fail.

### The Problem
If we commit to PostgreSQL first and then attempt to publish to Kafka, a failure in the second step results in data inconsistency (the training exists in the DB, but other services never know about it).

### The Solution: Transactional Outbox
1.  **Local Transaction:** When a `Training` is created, the service inserts the training data **AND** an `Event` record into a local `outbox` table within the same SQL transaction.
2.  **Commit:** The database guarantees both are saved atomically.
3.  **Relay:** A separate background process (or CDC connector like Debezium) reads the `outbox` table and pushes the messages to Kafka.

---

## 🔌 API Reference

### Base URL: `/api/v1`

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **POST** | `/trainings` | **Create Training.** Saves to DB and queues a `training_created` event. |
| **GET** | `/trainings/{id}` | **Get Training.** Returns full details (exercises, sets). |
| **PUT** | `/trainings/{id}` | **Update Training.** Updates state and queues a `training_updated` event. |
| **DELETE** | `/trainings/{id}` | **Delete Training.** Soft deletes and queues a `training_deleted` event. |

---

## 🔄 Event Integration

The service publishes all state changes to a single topic.

**Kafka Topic:** `training_events`

### Event Schema Example (`training_created`)

```json
{
  "event_id": "550e8400-e29b-41d4-a716-446655440000",
  "event_type": "training_created",
  "timestamp": "2026-01-05T12:00:00Z",
  "payload": {
    "training_id": "uuid-1234-5678",
    "user_id": "uuid-user-9876",
    "status": "COMPLETED",
    "exercises": [
      {
        "name": "Squat",
        "sets": [
          { "weight": 100, "reps": 5
          
          
          **📈 Data Model
The database schema reflects a hierarchical structure.

1. Training (Root Aggregate)
id (UUID, PK)

user_id (UUID, Index)

date (Timestamp)

status (Enum: PLANNED, IN_PROGRESS, COMPLETED)

2. Exercise
id (UUID, PK)

training_id (FK)

name (String)

notes (Text, Optional)

3. Set
id (UUID, PK)

exercise_id (FK)

weight (Decimal)

reps (Integer)

order (Integer) — To maintain the sequence of sets.**


# 🏋️ Training Service (Producer)

**Роль сервиса:** Main Data Source / System of Record (Основной источник данных)  
**Домен:** Управление тренировками и упражнениями

Этот микросервис выступает единым источником правды для всех данных о тренировках в системе. Он управляет жизненным циклом тренировочных сессий (CRUD) и асинхронно передает изменения состояния в другие сервисы (Аналитика, Геймификация, Уведомления) через Apache Kafka.

Для обеспечения целостности данных между базой данных и брокером сообщений сервис реализует паттерн **Transactional Outbox**.

---

## 🚀 Основные возможности

* **CRUD Тренировок:** Полное управление тренировочными сессиями (Создание, Чтение, Обновление, Удаление).
* **Детальный трекинг:** Глубокая вложенность данных: упражнения, подходы (сеты), рабочие веса, повторения и порядок выполнения.
* **Event Sourcing:** Реализация паттерна Publisher. Любое значимое изменение состояния генерирует событие в Kafka.
* **Надежность:** Гарантия доставки событий "At-Least-Once" (минимум один раз) благодаря использованию Transactional Outbox.

---

## 🛠 Технологический стек

| Компонент | Технология | Роль |
| :--- | :--- | :--- |
| **Язык** | Go / Java / Python | *Реализация сервиса* |
| **База данных** | **PostgreSQL** | *Основное персистентное хранилище* |
| **Брокер сообщений** | **Apache Kafka** | *Асинхронная шина событий* |
| **Архитектура** | **REST + Async Messaging** | *Гибридное взаимодействие* |
| **Паттерн** | **Transactional Outbox** | *Распределенная согласованность данных* |

---

## 🏗 Архитектура: Решение проблемы "Двойной записи"

Поскольку сервис является **Main Data Source**, он сталкивается с проблемой атомарности: запись в БД и публикация в Kafka должны либо выполниться вместе, либо не выполниться вовсе.

### Проблема
Если мы сначала сделаем коммит в PostgreSQL, а затем попытаемся отправить сообщение в Kafka, то при сбое на втором шаге возникнет рассинхронизация (тренировка есть в базе, но другие сервисы о ней не знают).

### Решение: Transactional Outbox
1.  **Локальная транзакция:** При создании тренировки сервис сохраняет данные о тренировке **И** запись о событии в специальную таблицу `outbox` в рамках одной SQL-транзакции.
2.  **Коммит:** База данных гарантирует, что оба действия выполнены атомарно.
3.  **Релей (Relay):** Отдельный фоновый процесс (или CDC-коннектор, например, Debezium) читает таблицу `outbox` и отправляет сообщения в Kafka.

---

## 🔌 API Reference

### Базовый URL: `/api/v1`

| Метод | Эндпоинт | Описание |
| :--- | :--- | :--- |
| **POST** | `/trainings` | **Создать тренировку.** Сохраняет в БД и ставит в очередь событие `training_created`. |
| **GET** | `/trainings/{id}` | **Получить тренировку.** Возвращает полные данные (включая упражнения и сеты). |
| **PUT** | `/trainings/{id}` | **Обновить тренировку.** Обновляет состояние и генерирует событие `training_updated`. |
| **DELETE** | `/trainings/{id}` | **Удалить тренировку.** Soft delete записи и генерация события `training_deleted`. |

---

## 🔄 События и Интеграция

Сервис публикует все изменения состояния в один топик.

**Kafka Topic:** `training_events`

### Пример схемы события (`training_created`)

```json
{
  "event_id": "550e8400-e29b-41d4-a716-446655440000",
  "event_type": "training_created",
  "timestamp": "2026-01-05T12:00:00Z",
  "payload": {
    "training_id": "uuid-1234-5678",
    "user_id": "uuid-user-9876",
    "status": "COMPLETED",
    "exercises": [
      {
        "name": "Приседания со штангой",
        "sets": [
          { "weight": 100, "reps": 5, "order": 1 },
          { "weight": 100, "reps": 5, "order": 2 }
        ]
      }
    ]
  }
}
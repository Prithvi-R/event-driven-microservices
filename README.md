# Event‑Driven Microservices Demo

## Project Overview

This repository contains a minimal **event‑driven microservices** example built with **Spring Boot**, **Kafka**, and **Redis**. It demonstrates:
- Asynchronous order processing via Kafka topics.
- Idempotent handling of order events in the inventory service.
- Dead‑letter queue (DLQ) handling and retry/back‑off.
- Simple circuit‑breaker with a fallback endpoint.
- Real‑time metrics (processed orders vs. DLQ orders).
- Docker‑Compose based development environment.

## Architecture

```
+----------------+      +----------------+      +-------------------+
|  Order Service | -->  |  Kafka Topic   | -->  | Inventory Service |
| (8081)         |      | order-events   |      | (8082)            |
+----------------+      +----------------+      +-------------------+
       |                      |                             |
       |                      |                             |
       |            +-------------------------------+       |
       +------------|  Kafka DLQ (order‑events.DLT) |       |
                    +-------------------------------+       |
                                                            |
                      +----------------+                    |
                      |   API Gateway  | <------------------+
                      | (8080)         |
                      +----------------+
```

- **Order Service** publishes `OrderEvent` messages to `order-events`.
- **Inventory Service** consumes those events, updates stock, and records processed order IDs in Redis for idempotency.
- On processing failure, the message is retried with back‑off; after max retries it is sent to the **DLQ**.
- A **DLQ consumer** logs the event and increments a metric.
- **API Gateway** routes external HTTP requests to the appropriate service and provides a fallback endpoint (`/fallback/orders`).

## Services

| Service | Port | Primary responsibilities |
|---------|------|---------------------------|
| **api-gateway** | 8080 | Routes requests, applies rate‑limiting & circuit‑breaker, exposes health & fallback. |
| **order-service** | 8081 | Accepts `POST /api/v1/orders` and publishes to Kafka. |
| **inventory-service** | 8082 | Provides stock endpoints, consumes order events, maintains idempotency, metrics. |

## Running the Project

```bash
# From the repository root
docker compose up -d --build
```

All services start and are reachable via `localhost`:
- Order Service: `http://localhost:8081`
- Inventory Service: `http://localhost:8082`
- API Gateway: `http://localhost:8080`

## Health Checks

```bash
# Order Service
docker exec order-service curl -s http://localhost:8081/api/v1/orders/health

# Inventory Service
docker exec inventory-service curl -s http://localhost:8082/api/v1/inventory/health

# API Gateway (Spring Actuator)
curl -s http://localhost:8080/actuator/health
```

## API Endpoints

| Method | URL | Description |
|--------|-----|-------------|
| `POST` | `/api/v1/orders` (order-service) | Create a new order. Returns the order with status `PENDING`. |
| `GET` | `/api/v1/inventory/{productId}` (inventory-service) | Get stock for a single product. |
| `GET` | `/api/v1/inventory` | **New** – list *all* product stocks. |
| `GET` | `/api/v1/inventory/processed-orders` | **New** – list processed order IDs and total count. |
| `GET` | `/api/v1/inventory/metrics` | Processed vs DLQ order metrics. |
| `GET` | `/fallback/orders` (api‑gateway) | Fallback endpoint (returns HTTP 503 JSON). |

## Command Cheat‑Sheet

See the companion file **COMMANDS.md** for copy‑and‑paste ready commands for each phase of testing.


# Command Cheat‑Sheet for Event‑Driven Microservices Demo

## Health Checks
```bash
# Order Service health
docker exec order-service curl -s http://localhost:8081/api/v1/orders/health

# Inventory Service health
docker exec inventory-service curl -s http://localhost:8082/api/v1/inventory/health

# API Gateway health (Spring Actuator)
curl -s http://localhost:8080/actuator/health
```

## Create an Order (PowerShell quoting)
```bash
# Replace <orderId>, <productId>, <quantity>, <price> as needed
docker exec order-service curl -s -X POST http://localhost:8081/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{"orderId":"ORD-1001","productId":"PROD-101","quantity":10,"price":250.0}'
```

## Query Stock for a Single Product
```bash
docker exec inventory-service curl -s http://localhost:8082/api/v1/inventory/PROD-101
```

## List All Product Stocks (new endpoint)
```bash
docker exec inventory-service curl -s http://localhost:8082/api/v1/inventory
```

## List Processed Order IDs (new endpoint)
```bash
docker exec inventory-service curl -s http://localhost:8082/api/v1/inventory/processed-orders
```

## View Metrics (processed vs DLQ orders)
```bash
docker exec inventory-service curl -s http://localhost:8082/api/v1/inventory/metrics
```

## Trigger Fallback (circuit‑breaker)
```bash
# Shows full HTTP response including status line
curl -i http://localhost:8080/fallback/orders
```

## Observe DLQ (if a poisonous order was sent)
```bash
# Inspect DLQ consumer logs (container name may vary)
docker logs inventory-service | grep DLQ
```

## Clean Up & Restart Services
```bash
docker compose down
docker compose up -d --build
```

*All commands are ready‑to‑copy. Adjust IDs or quantities as required for your test scenarios.*

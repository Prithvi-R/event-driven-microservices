package com.example.inventory_services.controller;

import com.example.inventory_services.model.InventoryItem;
import com.example.inventory_services.service.InventoryService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final StringRedisTemplate redisTemplate;

    public InventoryController(InventoryService inventoryService, StringRedisTemplate redisTemplate) {
        this.inventoryService = inventoryService;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryItem> getStock(@PathVariable String productId) {
        InventoryItem item = inventoryService.getProductStock(productId);
        return ResponseEntity.ok(item);
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("processedOrders", redisTemplate.opsForValue().get("metrics:orders:processed_count"));
        metrics.put("dlqOrders", redisTemplate.opsForValue().get("metrics:orders:dlq_count"));
        return ResponseEntity.ok(metrics);
    }

    @GetMapping
    public ResponseEntity<Map<String, InventoryItem>> getAllStocks() {
        return ResponseEntity.ok(inventoryService.getAllStock());
    }

    @GetMapping("/processed-orders")
    public ResponseEntity<Map<String, Object>> getProcessedOrders() {
        java.util.Set<String> keys = redisTemplate.keys("idempotency:order:*");
        java.util.List<String> orderIds = new java.util.ArrayList<>();
        if (keys != null) {
            for (String k : keys) {
                orderIds.add(k.replace("idempotency:order:", ""));
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("totalProcessed", orderIds.size());
        result.put("orderIds", orderIds);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Inventory Service is operational");
    }
}

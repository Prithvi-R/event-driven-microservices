package com.example.inventory_services.service;

import com.example.inventory_services.model.InventoryItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.Map;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);
    private final StringRedisTemplate redisTemplate;

    // Simulated primary database storage for high-throughput reads/writes demonstration
    private final Map<String, InventoryItem> databaseSimulation = new ConcurrentHashMap<>();

    public InventoryService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        // Seed mock initial stock
        databaseSimulation.put("PROD-101", new InventoryItem("PROD-101", 100, 0));
        databaseSimulation.put("PROD-102", new InventoryItem("PROD-102", 50, 0));
    }

    /**
     * Idempotency Check using Redis
     */
    public boolean isAlreadyProcessed(String orderId) {
        String key = "idempotency:order:" + orderId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void markAsProcessed(String orderId) {
        String key = "idempotency:order:" + orderId;
        redisTemplate.opsForValue().set(key, "PROCESSED", Duration.ofHours(24));
    }

    /**
     * Redis Multi-Layer Caching:
     * First hit checks Redis cache. On cache-miss, reads from DB and caches for subsequent requests.
     */
    @Cacheable(value = "inventory", key = "#productId")
    public InventoryItem getProductStock(String productId) {
        log.info(" Cache Miss! Fetching product {} directly from database layer...", productId);
        // Simulate DB latency
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {}

        return databaseSimulation.computeIfAbsent(productId, id -> new InventoryItem(id, 20, 0));
    }

    @CacheEvict(value = "inventory", key = "#productId")
    public void deductStock(String productId, int quantity) {
        InventoryItem item = databaseSimulation.computeIfAbsent(productId, id -> new InventoryItem(id, 100, 0));
        item.setAvailableQuantity(item.getAvailableQuantity() - quantity);
        item.setReservedQuantity(item.getReservedQuantity() + quantity);
        databaseSimulation.put(productId, item);
        log.info("Stock updated in DB for product {}: available={}, reserved={}", 
                productId, item.getAvailableQuantity(), item.getReservedQuantity());
    }

    public Map<String, InventoryItem> getAllStock() {
        return new HashMap<>(databaseSimulation);
    }
}

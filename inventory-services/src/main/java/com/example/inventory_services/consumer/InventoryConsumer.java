package com.example.inventory_services.consumer;

import com.example.inventory_services.model.OrderEvent;
import com.example.inventory_services.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryConsumer.class);

    private final InventoryService inventoryService;
    private final StringRedisTemplate redisTemplate;

    public InventoryConsumer(InventoryService inventoryService, StringRedisTemplate redisTemplate) {
        this.inventoryService = inventoryService;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Isolated consumer group: inventory-group
     * Reads from topic 'order-events'.
     * Demonstrates:
     * 1. Idempotency (prevent duplicate billing/stock reduction)
     * 2. Stock updates in Redis & primary store
     * 3. DLQ routing upon simulated poison-pill errors
     */
    @KafkaListener(topics = "order-events", groupId = "inventory-group", containerFactory = "kafkaListenerContainerFactory")
    public void handleOrderEvent(OrderEvent orderEvent) {
        log.info(" Received Order Event from Kafka: {}", orderEvent);

        if (orderEvent == null || orderEvent.getOrderId() == null) {
            log.warn("Received empty or malformed order event.");
            return;
        }

        // 1. Check if already processed
        if (inventoryService.isAlreadyProcessed(orderEvent.getOrderId())) {
            log.warn(" Duplicate event detected for orderId: {}. Skipping to ensure idempotency.", orderEvent.getOrderId());
            return;
        }

        // 2. Simulated failure to demonstrate Dead-Letter Queue (DLQ) if order has special product "POISON-PILL"
        if ("POISON-PILL".equalsIgnoreCase(orderEvent.getProductId())) {
            log.error(" Simulated critical processing failure for orderId: {}. Throwing exception to trigger DLQ!", orderEvent.getOrderId());
            throw new RuntimeException("Simulated processing failure for DLQ routing test!");
        }

        // 3. Mark processed for idempotency
        inventoryService.markAsProcessed(orderEvent.getOrderId());

        // 4. Process inventory deduction
        inventoryService.deductStock(orderEvent.getProductId(), orderEvent.getQuantity());
        redisTemplate.opsForValue().increment("metrics:orders:processed_count", 1);

        log.info(" Successfully processed order {} - reserved {} items of product {}",
                orderEvent.getOrderId(), orderEvent.getQuantity(), orderEvent.getProductId());
    }

    /**
     * Dead-Letter Topic (DLT) consumer to monitor / log failed messages routed by DeadLetterPublishingRecoverer
     */
    @KafkaListener(topics = "order-events.DLT", groupId = "inventory-dlq-group")
    public void handleDlqEvent(Object failedRecord) {
        log.error(" [DLQ CONSUMER] Alert! Dead-letter record received in 'order-events.DLT': {}", failedRecord);
        redisTemplate.opsForValue().increment("metrics:orders:dlq_count", 1);
    }
}

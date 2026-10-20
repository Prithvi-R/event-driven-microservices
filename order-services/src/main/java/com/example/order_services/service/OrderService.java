package com.example.order_services.service;

import com.example.order_services.model.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final String TOPIC = "order-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public OrderEvent createOrder(OrderEvent order) {
        order.setStatus("PENDING");
        
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(TOPIC, order.getOrderId(), order);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Order event published successfully for orderId: {} with offset: {}",
                        order.getOrderId(), result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish order event for orderId: {}", order.getOrderId(), ex);
            }
        });

        return order;
    }
}

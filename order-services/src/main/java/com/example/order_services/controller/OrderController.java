package com.example.order_services.controller;

import com.example.order_services.model.OrderEvent;
import com.example.order_services.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderEvent> placeOrder(@RequestBody OrderEvent order) {
        if (order.getOrderId() == null || order.getOrderId().isBlank()) {
            order.setOrderId(UUID.randomUUID().toString());
        }
        OrderEvent createdOrder = orderService.createOrder(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Order Service is operational");
    }
}

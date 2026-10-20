package com.example.apigateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/orders")
    public ResponseEntity<Map<String, Object>> orderFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "CIRCUIT_BREAKER_OPEN");
        response.put("message", "Order service is currently unavailable or high latency detected. Circuit breaker triggered fallback.");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/inventory")
    public ResponseEntity<Map<String, Object>> inventoryFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "CIRCUIT_BREAKER_OPEN");
        response.put("message", "Inventory service is experiencing high load or outage. Returning cached/degraded fallback.");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}

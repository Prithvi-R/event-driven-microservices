package com.example.inventory_services.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderEvent implements Serializable {
    private String orderId;
    private String productId;
    private int quantity;
    private double price;
    private String status;
}

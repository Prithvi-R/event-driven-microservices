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
public class InventoryItem implements Serializable {
    private String productId;
    private int availableQuantity;
    private int reservedQuantity;
}

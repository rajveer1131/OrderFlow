package com.example.OrderFlow.InventoryService.DTO.ResponseDTO;

import com.example.OrderFlow.InventoryService.Models.Category;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductResponseDTO {

    private Long id;

    private String sku;

    private String name;

    private String description;

    private BigDecimal price;

    private Integer stockQuantity;

    private Integer lowStockThreshold;

    private String category;

    private boolean active;
}

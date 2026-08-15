package com.example.OrderFlow.InventoryService.DTO.RequestDTO;

import com.example.OrderFlow.InventoryService.Models.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequestDTO {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private BigDecimal price;

    @NotNull
    @PositiveOrZero
    private Integer stockQty;

    @NotNull
    @PositiveOrZero
    private Integer lowStockThreshold;

    @NotNull
    private Long categoryId;

}

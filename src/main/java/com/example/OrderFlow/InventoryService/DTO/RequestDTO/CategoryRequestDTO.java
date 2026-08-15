package com.example.OrderFlow.InventoryService.DTO.RequestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CategoryRequestDTO {

    @NotBlank
    private String name;
    private String description;
}

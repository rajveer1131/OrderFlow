package com.example.OrderFlow.InventoryService.DTO.ResponseDTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryResponseDTO {

    private Long id;
    private String name;
    private String description;
}

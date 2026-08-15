package com.example.OrderFlow.InventoryService.DTO;

import com.example.OrderFlow.InventoryService.DTO.RequestDTO.CategoryRequestDTO;
import com.example.OrderFlow.InventoryService.DTO.ResponseDTO.CategoryResponseDTO;
import com.example.OrderFlow.InventoryService.Models.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponseDTO toResponse(Category category){
        return CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }

    public Category toEntity(CategoryRequestDTO categoryRequestDTO){
        return Category.builder()
                .name(categoryRequestDTO.getName())
                .description(categoryRequestDTO.getDescription())
                .build();
    }

}

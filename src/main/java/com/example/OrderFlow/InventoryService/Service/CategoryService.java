package com.example.OrderFlow.InventoryService.Service;

import com.example.OrderFlow.InventoryService.DTO.RequestDTO.CategoryRequestDTO;
import com.example.OrderFlow.InventoryService.DTO.ResponseDTO.CategoryResponseDTO;
import com.example.OrderFlow.InventoryService.Models.Category;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CategoryService {

    public CategoryResponseDTO createCategory(CategoryRequestDTO categoryRequestDTO);
    public CategoryResponseDTO updateCategory(Long id,CategoryRequestDTO categoryRequestDTO);
    public void deleteCategory(Long id);
    public CategoryResponseDTO getCategoryById(Long id);
    public List<CategoryResponseDTO> getAllCategories();
}

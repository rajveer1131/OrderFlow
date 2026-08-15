package com.example.OrderFlow.InventoryService.Service.impl;

import com.example.OrderFlow.InventoryService.DTO.CategoryMapper;
import com.example.OrderFlow.InventoryService.DTO.RequestDTO.CategoryRequestDTO;
import com.example.OrderFlow.InventoryService.DTO.ResponseDTO.CategoryResponseDTO;
import com.example.OrderFlow.InventoryService.Models.Category;
import com.example.OrderFlow.InventoryService.Repository.CategoryRepository;
import com.example.OrderFlow.InventoryService.Service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryRepository categoryRepository,CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }


    @Override
    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO categoryRequestDTO) {
        if(categoryRepository.existsByName(categoryRequestDTO.getName())){
            throw new IllegalArgumentException("Category Already Exists. Try with different name");
        }
        Category category = categoryMapper.toEntity(categoryRequestDTO);

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponseDTO updateCategory(Long id,CategoryRequestDTO categoryRequestDTO) {

        Category category = categoryRepository.findById(id).orElseThrow(
                ()->   new IllegalArgumentException("Category does not exists"));
        // TODO: Handle duplicate name correctly during update
        if(categoryRepository.existsByName(categoryRequestDTO.getName())){
            throw  new IllegalArgumentException("Category Already Exists. Try with different name");
        }
        category.setName(categoryRequestDTO.getName());
        category.setDescription(categoryRequestDTO.getDescription());
        return categoryMapper.toResponse(category);

    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        if(!categoryRepository.existsById(id)){
            throw  new IllegalArgumentException("Category does not exists");
        }
        // TODO: Need to check if any product uses this category first
        categoryRepository.deleteById(id);

    }

    @Override
    public CategoryResponseDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(
                ()->   new IllegalArgumentException("Category does not exists"));
        return categoryMapper.toResponse(category);
    }

    @Override
    public List<CategoryResponseDTO> getAllCategories() {

        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse).toList();
    }
}

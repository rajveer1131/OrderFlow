package com.example.OrderFlow.InventoryService.Service.impl;

import com.example.OrderFlow.Common.Exception.DuplicateResourceException;
import com.example.OrderFlow.Common.Exception.ResourceNotFoundException;
import com.example.OrderFlow.InventoryService.DTO.CategoryMapper;
import com.example.OrderFlow.InventoryService.DTO.RequestDTO.CategoryRequestDTO;
import com.example.OrderFlow.InventoryService.DTO.ResponseDTO.CategoryResponseDTO;
import com.example.OrderFlow.InventoryService.Models.Category;
import com.example.OrderFlow.InventoryService.Repository.CategoryRepository;
import com.example.OrderFlow.InventoryService.Repository.ProductRepository;
import com.example.OrderFlow.InventoryService.Service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final ProductRepository productRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository,CategoryMapper categoryMapper,ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.productRepository = productRepository;
    }


    @Override
    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO categoryRequestDTO) {
        if(categoryRepository.existsByName(categoryRequestDTO.getName())){
            throw new DuplicateResourceException("Category Already Exists. Try with different name");
        }
        Category category = categoryMapper.toEntity(categoryRequestDTO);

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponseDTO updateCategory(Long id,CategoryRequestDTO categoryRequestDTO) {

        Category category = categoryRepository.findById(id).orElseThrow(
                ()->   new ResourceNotFoundException("Category does not exists with id: "+id));
        // TODO: Handle duplicate name correctly during update
        if(categoryRepository.existsByName(categoryRequestDTO.getName())){
            throw  new DuplicateResourceException("Category Already Exists. Try with different name");
        }
        category.setName(categoryRequestDTO.getName());
        category.setDescription(categoryRequestDTO.getDescription());
        return categoryMapper.toResponse(category);

    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        if(!categoryRepository.existsById(id)){
            throw  new ResourceNotFoundException("Category does not exists with id: "+id);
        }
        // TODO: Need to check if any product uses this category first
        if(productRepository.existsByCategoryId(id)){
            throw new IllegalStateException(
                    "Products with this category exists"
            );
        }
        categoryRepository.deleteById(id);

    }

    @Override
    public CategoryResponseDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(
                ()->   new ResourceNotFoundException("Category does not exists with id: "+id));
        return categoryMapper.toResponse(category);
    }

    @Override
    public List<CategoryResponseDTO> getAllCategories() {

        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse).toList();
    }
}

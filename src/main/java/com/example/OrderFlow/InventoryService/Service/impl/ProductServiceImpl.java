package com.example.OrderFlow.InventoryService.Service.impl;

import com.example.OrderFlow.Common.Exception.DuplicateResourceException;
import com.example.OrderFlow.Common.Exception.ResourceNotFoundException;
import com.example.OrderFlow.InventoryService.DTO.ProductMapper;
import com.example.OrderFlow.InventoryService.DTO.RequestDTO.ProductRequestDTO;
import com.example.OrderFlow.InventoryService.DTO.ResponseDTO.ProductResponseDTO;
import com.example.OrderFlow.InventoryService.Models.Category;
import com.example.OrderFlow.InventoryService.Models.Product;
import com.example.OrderFlow.InventoryService.Repository.CategoryRepository;
import com.example.OrderFlow.InventoryService.Repository.ProductRepository;
import com.example.OrderFlow.InventoryService.Service.CategoryService;
import com.example.OrderFlow.InventoryService.Service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(ProductRepository productRepository,ProductMapper productMapper,CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.categoryRepository = categoryRepository;
    }


    @Override
    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO) {
        Category category = categoryRepository.findById(productRequestDTO.getCategoryId())
                .orElseThrow(()->new ResourceNotFoundException("Category does not exist"));
        String sku = generateSku();

        Product product = productMapper.toEntity(productRequestDTO,sku,category);


        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO productRequestDTO) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product does not exist with id: "+id));

        Category category = categoryRepository.findById(productRequestDTO.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category does not exist with id: "+productRequestDTO.getCategoryId()));

        if (productRequestDTO.getName() != null) {
            product.setName(productRequestDTO.getName());
        }

        if (productRequestDTO.getDescription() != null) {
            product.setDescription(productRequestDTO.getDescription());
        }

        if (productRequestDTO.getPrice() != null) {
            product.setPrice(productRequestDTO.getPrice());
        }

        if (productRequestDTO.getStockQty() != null) {
            product.setStockQuantity(productRequestDTO.getStockQty());
        }

        if (productRequestDTO.getLowStockThreshold() != null) {
            product.setLowStockThreshold(productRequestDTO.getLowStockThreshold());
        }

        product.setCategory(category);

        return productMapper.toResponse(product);
    }


    private String generateSku(){
        String generatedSku = "";
        int retry = 5;
        String alphaChars = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
        do{
            StringBuilder sku = new StringBuilder("PRD-");
            retry--;

        for(int i=0;i<8;i++){
            int rand = (int)(Math.random()*alphaChars.length());
            sku.append(alphaChars.charAt(rand));
        }

        generatedSku = sku.toString();
        }while(productRepository.existsBySku(generatedSku) && retry>0);
        if (retry == 0 && productRepository.existsBySku(generatedSku)) {
            throw new DuplicateResourceException("Unable to generate unique SKU");
        }
        return generatedSku;
    }

    @Override
    public ProductResponseDTO getProductById(Long id) {

        Product product = productRepository.findById(id).orElseThrow(
                ()-> new ResourceNotFoundException("Product does not exist with id: "+id));
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id).orElseThrow(
                ()->   new ResourceNotFoundException("Product does not exist with id: "+id));
        product.setActive(false);
    }

    @Override
    public List<ProductResponseDTO> getAllProducts() {

        return productRepository.findAll().stream()
                .map(productMapper::toResponse)
                .toList();
    }


    @Override
    public ProductResponseDTO getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku).orElseThrow(()->new ResourceNotFoundException("Product does not exist with sku: "+sku));
        return productMapper.toResponse(product);
    }

    @Override
    public List<ProductResponseDTO> getProductsByCategoryId(Long categoryId) {
        return productRepository.findByCategoryId(categoryId).stream()
                .map(productMapper::toResponse).toList();
    }

    @Override
    public Product getProductEntityById(Long id) {

        return  productRepository.findById(id).orElseThrow(
                ()-> new ResourceNotFoundException("Product does not exist with id: "+id));
    }

    @Override
    @Transactional
    public void restoreStock(Long id, Integer stockQty) {
        Product product = productRepository.findById(id).orElseThrow(
                ()-> new ResourceNotFoundException("Product does not exist with id: "+id));
        product.setStockQuantity(product.getStockQuantity()+stockQty);

    }
}

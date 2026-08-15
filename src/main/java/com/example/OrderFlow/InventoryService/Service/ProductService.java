package com.example.OrderFlow.InventoryService.Service;

import com.example.OrderFlow.InventoryService.DTO.ProductMapper;
import com.example.OrderFlow.InventoryService.DTO.RequestDTO.ProductRequestDTO;
import com.example.OrderFlow.InventoryService.DTO.ResponseDTO.ProductResponseDTO;
import com.example.OrderFlow.InventoryService.Models.Product;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ProductService {

    public ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO);
    public ProductResponseDTO updateProduct(Long id,ProductRequestDTO productRequestDTO);
    public ProductResponseDTO getProductById(Long id);
    public void deleteProduct(Long id);
    public List<ProductResponseDTO> getAllProducts();

    public ProductResponseDTO getProductBySku(String sku);

    public List<ProductResponseDTO> getProductsByCategoryId(Long categoryId);
    public Product getProductEntityById(Long prodId);
    public void restoreStock(Long id,Integer stockQty);
}

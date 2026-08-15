package com.example.OrderFlow.InventoryService.DTO;

import com.example.OrderFlow.InventoryService.DTO.RequestDTO.ProductRequestDTO;
import com.example.OrderFlow.InventoryService.DTO.ResponseDTO.ProductResponseDTO;
import com.example.OrderFlow.InventoryService.Models.Category;
import com.example.OrderFlow.InventoryService.Models.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponseDTO toResponse(Product product){
        return ProductResponseDTO.builder()
                .name(product.getName())
                .sku(product.getSku())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .lowStockThreshold(product.getLowStockThreshold())
                .id(product.getId())
                .category(product.getCategory().getName())
                .active(product.isActive())
                .build();
    }

    public Product toEntity(
            ProductRequestDTO productRequestDTO,
            String sku,
            Category category
    ){
        return Product.builder()
                .lowStockThreshold(productRequestDTO.getLowStockThreshold())
                .category(category)
                .name(productRequestDTO.getName())
                .price(productRequestDTO.getPrice())
                .stockQuantity(productRequestDTO.getStockQty())
                .description(productRequestDTO.getDescription())
                .sku(sku)
                .build();

    }
}

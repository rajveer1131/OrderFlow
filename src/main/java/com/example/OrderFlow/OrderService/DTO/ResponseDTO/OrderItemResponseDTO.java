package com.example.OrderFlow.OrderService.DTO.ResponseDTO;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Builder
@Data
public class OrderItemResponseDTO {

    private Long id;
    private Long productId;
    private String sku;
    private String productName;
    private BigDecimal priceAtOrder;
    private Integer  quantity;
    private BigDecimal subTotal;
}

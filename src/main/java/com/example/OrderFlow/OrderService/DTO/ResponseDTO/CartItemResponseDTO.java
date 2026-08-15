package com.example.OrderFlow.OrderService.DTO.ResponseDTO;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Builder
@Data
public class CartItemResponseDTO {

    private Long id;

    private Long productId;

    private String productName;

    private String sku;

    private BigDecimal unitPrice;

    private Integer quantity;

    private BigDecimal subTotal;

}

package com.example.OrderFlow.OrderService.DTO.ResponseDTO;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CartResponseDTO {

    private Long id;
    private List<CartItemResponseDTO> items;
    private BigDecimal totalAmount;
}

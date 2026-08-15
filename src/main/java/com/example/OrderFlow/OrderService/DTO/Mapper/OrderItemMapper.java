package com.example.OrderFlow.OrderService.DTO.Mapper;

import com.example.OrderFlow.OrderService.DTO.ResponseDTO.OrderItemResponseDTO;
import com.example.OrderFlow.OrderService.Model.OrderItem;
import org.springframework.stereotype.Component;

@Component
public class OrderItemMapper {

    public OrderItemResponseDTO toResponse(OrderItem orderItem) {
        return OrderItemResponseDTO.builder()
                .id(orderItem.getId())
                .productId(orderItem.getProductId())
                .productName(orderItem.getProductName())
                .sku(orderItem.getSku())
                .priceAtOrder(orderItem.getPriceAtOrder())
                .quantity(orderItem.getQuantity())
                .subTotal(orderItem.getSubTotal())
                .build();
    }
}
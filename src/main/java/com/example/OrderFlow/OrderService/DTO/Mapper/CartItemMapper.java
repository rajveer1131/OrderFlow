package com.example.OrderFlow.OrderService.DTO.Mapper;

import com.example.OrderFlow.OrderService.DTO.ResponseDTO.CartItemResponseDTO;
import com.example.OrderFlow.OrderService.Model.CartItem;
import org.springframework.stereotype.Component;

@Component
public class CartItemMapper {
    public CartItemResponseDTO toResponse(CartItem cartItem){
        return CartItemResponseDTO.builder()
                .productName(cartItem.getProduct().getName())
                .sku(cartItem.getProduct().getSku())
                .id(cartItem.getId())
                .productId(cartItem.getProduct().getId())
                .unitPrice(cartItem.getProduct().getPrice())
                .subTotal(cartItem.getSubTotal())
                .quantity(cartItem.getQuantity())
                .build();
    }
}

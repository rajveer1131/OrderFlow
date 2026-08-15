package com.example.OrderFlow.OrderService.DTO.Mapper;

import com.example.OrderFlow.OrderService.DTO.ResponseDTO.CartResponseDTO;
import com.example.OrderFlow.OrderService.Model.Cart;
import org.springframework.stereotype.Component;


@Component
public class CartMapper {

    private final CartItemMapper cartItemMapper;
    public CartMapper(CartItemMapper cartItemMapper){
        this.cartItemMapper = cartItemMapper;
    }

    public CartResponseDTO toResponse(Cart cart){
        return CartResponseDTO.builder()
                .id(cart.getId())
                .items(cart.getItems().stream().map(cartItemMapper::toResponse).toList())
                .totalAmount(cart.getTotalAmount())
                .build();
    }
}

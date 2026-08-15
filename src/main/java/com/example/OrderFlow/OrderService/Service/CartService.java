package com.example.OrderFlow.OrderService.Service;

import com.example.OrderFlow.OrderService.DTO.RequestDTO.CartItemRequestDTO;
import com.example.OrderFlow.OrderService.DTO.ResponseDTO.CartResponseDTO;
import com.example.OrderFlow.OrderService.Model.Cart;
import com.example.OrderFlow.OrderService.Model.CartItem;

import java.math.BigDecimal;

public interface CartService {
    CartResponseDTO getCartByUserId(Long userId);
    Cart getCartForCheckOut(Long userId);
    CartResponseDTO addItemToCart(Long userId, CartItemRequestDTO cartItemRequestDTO);
    CartResponseDTO updateItemQuantity(Long userId, CartItemRequestDTO cartItemRequestDTO);
    CartResponseDTO removeItemFromCart(Long userId, Long productId);
    void clearCart(Long userId);
}

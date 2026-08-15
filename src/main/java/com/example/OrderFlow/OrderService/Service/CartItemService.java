package com.example.OrderFlow.OrderService.Service;

import com.example.OrderFlow.OrderService.DTO.ResponseDTO.CartItemResponseDTO;
import com.example.OrderFlow.OrderService.Model.CartItem;

public interface CartItemService {
    CartItemResponseDTO getCartItemById(Long id);
    void deleteCartItem(Long id);
}

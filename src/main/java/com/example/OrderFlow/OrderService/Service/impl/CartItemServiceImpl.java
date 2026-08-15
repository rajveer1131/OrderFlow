package com.example.OrderFlow.OrderService.Service.impl;

import com.example.OrderFlow.OrderService.DTO.Mapper.CartItemMapper;
import com.example.OrderFlow.OrderService.DTO.ResponseDTO.CartItemResponseDTO;
import com.example.OrderFlow.OrderService.Model.CartItem;
import com.example.OrderFlow.OrderService.Repository.CartItemRepository;
import com.example.OrderFlow.OrderService.Service.CartItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;
    private final CartItemMapper cartItemMapper;

    public CartItemServiceImpl(CartItemRepository cartItemRepository,CartItemMapper cartItemMapper) {
        this.cartItemRepository = cartItemRepository;
        this.cartItemMapper = cartItemMapper;
    }

    @Override
    public CartItemResponseDTO getCartItemById(Long id) {
        CartItem cartItem = cartItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CartItem not found with id: " + id));
        return cartItemMapper.toResponse(cartItem);
    }

    @Override
    @Transactional
    public void deleteCartItem(Long id) {

        if(cartItemRepository.existsById(id)){
            cartItemRepository.deleteById(id);
        }else{
            throw new IllegalArgumentException("CartItem not found with id: " + id);
        }


    }
}

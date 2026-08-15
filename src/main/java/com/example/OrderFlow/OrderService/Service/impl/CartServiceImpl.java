package com.example.OrderFlow.OrderService.Service.impl;

import com.example.OrderFlow.InventoryService.Models.Product;
import com.example.OrderFlow.InventoryService.Repository.ProductRepository;
import com.example.OrderFlow.InventoryService.Service.ProductService;
import com.example.OrderFlow.OrderService.DTO.Mapper.CartMapper;
import com.example.OrderFlow.OrderService.DTO.RequestDTO.CartItemRequestDTO;
import com.example.OrderFlow.OrderService.DTO.ResponseDTO.CartItemResponseDTO;
import com.example.OrderFlow.OrderService.DTO.ResponseDTO.CartResponseDTO;
import com.example.OrderFlow.OrderService.Model.Cart;
import com.example.OrderFlow.OrderService.Model.CartItem;
import com.example.OrderFlow.OrderService.Repository.CartItemRepository;
import com.example.OrderFlow.OrderService.Repository.CartRepository;
import com.example.OrderFlow.OrderService.Service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;

    public CartServiceImpl(CartRepository cartRepository,
                           CartItemRepository cartItemRepository,
                           ProductRepository productRepository,
                           CartMapper cartMapper) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.cartMapper = cartMapper;
    }

    @Override
    @Transactional
    public CartResponseDTO getCartByUserId(Long userId) {

        Cart cart = getOrCreateCartByUserId(userId);

        return cartMapper.toResponse(cart);
    }

    @Override
    public Cart getCartForCheckOut(Long userId) {
        return getOrCreateCartByUserId(userId);
    }

    @Override
    @Transactional
    public CartResponseDTO addItemToCart(Long userId,CartItemRequestDTO cartItemRequestDTO) {

        Product product = productRepository.findById(cartItemRequestDTO.getProductId()).orElseThrow(
                ()-> new IllegalArgumentException("Product does not exist"));
        if(!product.isActive()){
            throw new IllegalArgumentException("Product is not active");
        }
        if (cartItemRequestDTO.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }


        Cart cart = getOrCreateCartByUserId(userId);


        Optional<CartItem> existingItem =
                cartItemRepository.findByCartIdAndProductId(
                        cart.getId(),
                        product.getId()
                );

        if (existingItem.isPresent()) {

            CartItem cartItem = existingItem.get();

            int newQuantity =
                    cartItem.getQuantity() + cartItemRequestDTO.getQuantity();

            if (newQuantity > product.getStockQuantity()) {
                throw new IllegalArgumentException("Insufficient stock");
            }

            cartItem.setQuantity(newQuantity);
            cartItem.calculateSubTotal();

        } else {

            if (cartItemRequestDTO.getQuantity() > product.getStockQuantity()) {
                throw new IllegalArgumentException("Insufficient stock");
            }

            CartItem cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setQuantity(cartItemRequestDTO.getQuantity());
            cartItem.calculateSubTotal();

            cart.addItem(cartItem);
        }

        cart.recalculateTotal();

        cartRepository.flush();

        return cartMapper.toResponse(cart);

    }

    private Cart getOrCreateCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(Cart.builder()
                        .userId(userId)
                        .totalAmount(BigDecimal.ZERO)
                        .build()));
    }

    @Override
    @Transactional
    public CartResponseDTO updateItemQuantity(Long userId, CartItemRequestDTO cartItemRequestDTO) {
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(
                ()-> new IllegalArgumentException("Cart by user does not exist")
        );
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), cartItemRequestDTO.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found in cart: " + cartItemRequestDTO.getProductId()));

        if (cartItemRequestDTO.getQuantity() <= 0) {
            cart.removeItem(item);

        }
        else if(cartItemRequestDTO.getQuantity()> item.getProduct().getStockQuantity()){
            throw new IllegalArgumentException("Product quantity is less than specified quantity");
        }
        else {
            item.setQuantity(cartItemRequestDTO.getQuantity());
            item.calculateSubTotal();

        }

        cart.recalculateTotal();
        return cartMapper.toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponseDTO removeItemFromCart(Long userId, Long productId) {
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(
                ()-> new IllegalArgumentException("Cart by user does not exist")
        );
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new RuntimeException("Product not found in cart: " + productId));

        cart.removeItem(item);

        cart.recalculateTotal();
        return cartMapper.toResponse(cart);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(
                ()-> new IllegalArgumentException("Cart by user does not exist")
        );
        cart.clearItems();
        cart.recalculateTotal();


    }


}

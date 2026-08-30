package com.example.OrderFlow.OrderService.Controller;

import com.example.OrderFlow.Config.Security.CustomUserDetails;
import com.example.OrderFlow.OrderService.DTO.RequestDTO.CartItemRequestDTO;
import com.example.OrderFlow.OrderService.Model.Cart;
import com.example.OrderFlow.OrderService.Service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<?> getCartByUserId(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                cartService.getCartByUserId(userDetails.getUserId())
        );
    }

    @PostMapping("/items")
    public ResponseEntity<?> addItemToCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CartItemRequestDTO cartItemRequestDTO
    ) {
        return ResponseEntity.ok(
                cartService.addItemToCart(
                        userDetails.getUserId(),
                        cartItemRequestDTO
                )
        );
    }

    @PutMapping("/items")
    public ResponseEntity<?> updateItemQuantity(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CartItemRequestDTO cartItemRequestDTO
    ) {
        return ResponseEntity.ok(
                cartService.updateItemQuantity(
                        userDetails.getUserId(),
                        cartItemRequestDTO
                )
        );
    }

    @DeleteMapping("/items")
    public ResponseEntity<?> removeItemFromCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long productId
    ) {
        return ResponseEntity.ok(
                cartService.removeItemFromCart(
                        userDetails.getUserId(),
                        productId
                )
        );
    }

    @DeleteMapping
    public ResponseEntity<?> clearCart(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        cartService.clearCart(userDetails.getUserId());

        return ResponseEntity.ok("Cart cleared successfully");
    }
}
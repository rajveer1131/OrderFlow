package com.example.OrderFlow.OrderService.Controller;

import com.example.OrderFlow.OrderService.DTO.RequestDTO.CartItemRequestDTO;
import com.example.OrderFlow.OrderService.Model.Cart;
import com.example.OrderFlow.OrderService.Service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<?> getCartByUserId(@RequestParam Long userId) {
        return ResponseEntity.ok(cartService.getCartByUserId(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<?> addItemToCart(@RequestParam Long userId,
                                            @Valid @RequestBody  CartItemRequestDTO cartItemRequestDTO) {

        return ResponseEntity.ok(cartService.addItemToCart(userId,cartItemRequestDTO));
    }

    @PutMapping("/items")
    public ResponseEntity<?> updateItemQuantity(@RequestParam Long userId,
                                                  @Valid @RequestBody  CartItemRequestDTO cartItemRequestDTO) {

        return ResponseEntity.ok(cartService.updateItemQuantity(userId,cartItemRequestDTO));
    }

    @DeleteMapping("/items")
    public ResponseEntity<?> removeItemFromCart(@RequestParam Long userId,
                                                   @RequestParam Long productId) {
        return ResponseEntity.ok(cartService.removeItemFromCart(userId, productId));
    }

    @DeleteMapping
    public ResponseEntity<?> clearCart(@RequestParam Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok("Cart deleted successfully");
    }
}

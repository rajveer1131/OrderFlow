package com.example.OrderFlow.OrderService.Controller;

import com.example.OrderFlow.Config.Security.CustomUserDetails;
import com.example.OrderFlow.OrderService.Model.OrderStatus;
import com.example.OrderFlow.OrderService.Service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkoutCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long shippingAddressId
    ) {
        return ResponseEntity.ok(
                orderService.checkout(
                        userDetails.getUserId(),
                        shippingAddressId
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                orderService.getOrderByIdForUser(
                        id,
                        userDetails.getUserId()
                )
        );
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<?> getOrderByOrderNumber(
            @PathVariable String orderNumber,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                orderService.getOrderByOrderNumberForUser(
                        orderNumber,
                        userDetails.getUserId()
                )
        );
    }

    @GetMapping
    public ResponseEntity<?> getMyOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                orderService.getOrdersByUserId(
                        userDetails.getUserId()
                )
        );
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                orderService.cancelOrder(id,
                        userDetails.getUserId()
                )
        );
    }
}
package com.example.OrderFlow.OrderService.Service;

import com.example.OrderFlow.OrderService.DTO.ResponseDTO.OrderResponseDTO;
import com.example.OrderFlow.OrderService.Model.Order;
import com.example.OrderFlow.OrderService.Model.OrderStatus;

import java.util.List;

public interface OrderService {
    OrderResponseDTO checkout(Long userId, Long shippingAddressId);
    OrderResponseDTO getOrderById(Long id);
    OrderResponseDTO getOrderByOrderNumber(String orderNumber);
    List<OrderResponseDTO> getOrdersByUserId(Long userId);
    List<OrderResponseDTO> getOrdersByStatus(OrderStatus status);
    OrderResponseDTO updateOrderStatus(Long orderId, OrderStatus status);
    OrderResponseDTO cancelOrder(Long orderId);
}

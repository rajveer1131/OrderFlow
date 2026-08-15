package com.example.OrderFlow.OrderService.DTO.Mapper;


import com.example.OrderFlow.OrderService.DTO.ResponseDTO.OrderResponseDTO;
import com.example.OrderFlow.OrderService.Model.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    private final OrderItemMapper orderItemMapper;

    public OrderMapper(OrderItemMapper orderItemMapper){
        this.orderItemMapper = orderItemMapper;
    }

    public OrderResponseDTO toResponse(Order order){
        return OrderResponseDTO.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .shippingAddress(order.getShippingAddress())
                .paymentStatus(order.getPaymentStatus().name())
                .orderNumber(order.getOrderNumber())
                .orderStatus(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .items(order.getItems().stream().map(orderItemMapper::toResponse).toList())
                .build();
    }
}

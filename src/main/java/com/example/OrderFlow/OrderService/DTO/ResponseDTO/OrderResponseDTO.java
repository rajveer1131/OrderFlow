package com.example.OrderFlow.OrderService.DTO.ResponseDTO;

import com.example.OrderFlow.OrderService.Model.OrderShippingAddress;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Data
public class OrderResponseDTO {

    private Long id;
    private String orderNumber;
    private Long userId;
    private OrderShippingAddress shippingAddress;
    private String orderStatus;
    private String paymentStatus;
    private BigDecimal totalAmount;
    private List<OrderItemResponseDTO> items;

}

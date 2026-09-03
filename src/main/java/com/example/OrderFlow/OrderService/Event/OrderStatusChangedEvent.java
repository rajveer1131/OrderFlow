package com.example.OrderFlow.OrderService.Event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusChangedEvent {

    private Long orderId;
    private Long userId;
    private String email;
    private String orderNumber;
    private String orderStatus;
}
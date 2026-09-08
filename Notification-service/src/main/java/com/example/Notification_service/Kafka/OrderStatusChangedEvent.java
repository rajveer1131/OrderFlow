package com.example.Notification_service.Kafka;

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

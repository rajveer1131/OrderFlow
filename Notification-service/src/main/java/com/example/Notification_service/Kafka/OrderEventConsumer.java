package com.example.Notification_service.Kafka;

import com.example.Notification_service.service.NotificationService;
import jakarta.mail.MessagingException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventConsumer {

    public static final String ORDER_STATUS_TOPIC = "order-status-events";

    private final NotificationService notificationService;

    public OrderEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(
            topics = ORDER_STATUS_TOPIC,
            groupId = "notification-service"
    )
    public void topicConsumer(OrderStatusChangedEvent event) throws MessagingException {
        notificationService.SendNotification(event);

    }
}
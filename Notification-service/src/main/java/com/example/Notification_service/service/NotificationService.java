package com.example.Notification_service.service;

import com.example.Notification_service.Kafka.OrderStatusChangedEvent;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;

@Service
public interface NotificationService {

    void SendNotification(OrderStatusChangedEvent event) throws MessagingException;

}

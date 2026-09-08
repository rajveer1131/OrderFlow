package com.example.Notification_service.service.impl;

import com.example.Notification_service.Kafka.OrderStatusChangedEvent;
import com.example.Notification_service.service.EmailService;
import com.example.Notification_service.service.NotificationService;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final EmailService emailService;

    NotificationServiceImpl(EmailService emailService){
        this.emailService = emailService;
    }

    @Override
    public void SendNotification(OrderStatusChangedEvent event) throws MessagingException {
        if ("CONFIRMED".equals(event.getOrderStatus())) {

            emailService.sendOrderConfirmation(
                    event.getEmail(),
                    event.getOrderNumber()
            );
        }

    }
}

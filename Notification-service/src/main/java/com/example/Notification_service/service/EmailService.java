package com.example.Notification_service.service;

import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;

@Service
public interface EmailService {
    void sendOrderConfirmation(String to, String orderNumber) throws MessagingException;
}

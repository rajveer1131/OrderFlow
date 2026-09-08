package com.example.Notification_service.service.impl;

import com.example.Notification_service.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    EmailServiceImpl(JavaMailSender mailSender){
        this.mailSender = mailSender;
    }
    @Override
    public void sendOrderConfirmation(String to, String orderNumber) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        mimeMessageHelper.setTo(to);
        mimeMessageHelper.setSubject("Order Confirmed - " + orderNumber);



        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Order Confirmed</title>
                </head>
                
                <body style="margin:0; padding:0; background-color:#f4f4f4; font-family:Arial,sans-serif;">
                
                    <div style="max-width:600px; margin:40px auto; background:#ffffff;
                                border-radius:8px; overflow:hidden;">
                
                        <div style="padding:24px; text-align:center; background:#222222; color:#ffffff;">
                            <h1 style="margin:0;">Order Confirmed</h1>
                        </div>
                
                        <div style="padding:32px;">
                            <h2>Thank you for your order!</h2>
                
                            <p>
                                Your order has been successfully confirmed.
                            </p>
                
                            <div style="margin:24px 0; padding:16px; background:#f7f7f7;
                                        border-radius:6px;">
                                <p style="margin:0 0 8px 0;">
                                    <strong>Order Number:</strong> %s
                                </p>
                
                                <p style="margin:0;">
                                    <strong>Status:</strong> Confirmed
                                </p>
                            </div>
                
                            <p>
                                We will notify you when your order is shipped.
                            </p>
                
                            <p style="margin-top:32px;">
                                Regards,<br>
                                OrderFlow Team
                            </p>
                        </div>
                
                        <div style="padding:16px; text-align:center; background:#f4f4f4;
                                    color:#777777; font-size:12px;">
                            This is an automated email. Please do not reply.
                        </div>
                
                    </div>
                
                </body>
                </html>
                """.formatted(orderNumber);


        mimeMessageHelper.setText(html, true);
        mailSender.send(mimeMessage);
    }


}

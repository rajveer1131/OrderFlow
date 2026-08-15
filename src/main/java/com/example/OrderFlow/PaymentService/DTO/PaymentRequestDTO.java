package com.example.OrderFlow.PaymentService.DTO;

import com.example.OrderFlow.PaymentService.Model.PaymentMode;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Builder
@Data
public class PaymentRequestDTO {
    private Long orderId;
    private PaymentMode paymentMode;
    private BigDecimal paymentAmount;
    private boolean simulatedPayment;
}

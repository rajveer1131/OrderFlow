package com.example.OrderFlow.PaymentService.DTO;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Builder
@Data
public class PaymentResponseDTO {

    private Long id;
    private Long orderId;
    private BigDecimal paymentAmount;
    private String paymentStatus;
    private String transactionReference;
}

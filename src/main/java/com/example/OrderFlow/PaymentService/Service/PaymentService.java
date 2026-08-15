package com.example.OrderFlow.PaymentService.Service;

import com.example.OrderFlow.PaymentService.DTO.PaymentRequestDTO;
import com.example.OrderFlow.PaymentService.DTO.PaymentResponseDTO;
import com.example.OrderFlow.PaymentService.Model.PaymentMode;
import com.example.OrderFlow.PaymentService.Model.PaymentStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public interface PaymentService {

    public PaymentResponseDTO processPayment(PaymentRequestDTO paymentRequestDTO);

}

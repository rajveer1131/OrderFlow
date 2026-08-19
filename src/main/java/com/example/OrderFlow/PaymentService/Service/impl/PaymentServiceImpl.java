package com.example.OrderFlow.PaymentService.Service.impl;

import com.example.OrderFlow.Common.Exception.DuplicateResourceException;
import com.example.OrderFlow.PaymentService.DTO.PaymentRequestDTO;
import com.example.OrderFlow.PaymentService.DTO.PaymentResponseDTO;
import com.example.OrderFlow.PaymentService.Model.Payment;
import com.example.OrderFlow.PaymentService.Model.PaymentStatus;
import com.example.OrderFlow.PaymentService.Repository.PaymentRepository;
import com.example.OrderFlow.PaymentService.Service.PaymentService;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService {


    private  final PaymentRepository paymentRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository){
        this.paymentRepository = paymentRepository;
    }

    @Override
    public PaymentResponseDTO processPayment(PaymentRequestDTO paymentRequest) {


        PaymentStatus status = paymentRequest.isSimulatedPayment()?
                PaymentStatus.SUCCESS:PaymentStatus.FAILED;

        Payment payment = Payment.builder()
                .paymentStatus(status)
                .orderId(paymentRequest.getOrderId())
                .paymentAmount(paymentRequest.getPaymentAmount())
                .paymentMode(paymentRequest.getPaymentMode())
                .transactionReference(generateTransRef())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        return PaymentResponseDTO.builder()
                .paymentStatus(savedPayment.getPaymentStatus().name())
                .orderId(savedPayment.getOrderId())
                .transactionReference(savedPayment.getTransactionReference())
                .id(savedPayment.getId())
                .paymentAmount(savedPayment.getPaymentAmount())
                .build();
    }

    private String generateTransRef(){
        String generatedRef = "";
        int retry = 5;
        String alphaChars = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
        do{
            StringBuilder ref = new StringBuilder("PAY-");
            retry--;

            for(int i=0;i<8;i++){
                int rand = (int)(Math.random()*alphaChars.length());
                ref.append(alphaChars.charAt(rand));
            }

            generatedRef = ref.toString();
        }while(paymentRepository.existsByTransactionReference(generatedRef) && retry>0);
        if (retry == 0 && paymentRepository.existsByTransactionReference(generatedRef)) {
            throw new DuplicateResourceException("Unable to generate unique transaction reference");
        }
        return generatedRef;
    }
}

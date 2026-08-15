package com.example.OrderFlow.PaymentService.Repository;

import com.example.OrderFlow.PaymentService.Model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existByTransactionReference(String ref);
}

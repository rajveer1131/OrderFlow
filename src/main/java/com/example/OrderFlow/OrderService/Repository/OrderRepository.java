package com.example.OrderFlow.OrderService.Repository;

import com.example.OrderFlow.OrderService.Model.Order;
import com.example.OrderFlow.OrderService.Model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByUserId(Long userId);
    List<Order> findByStatus(OrderStatus status);
    boolean existsByOrderNumber(String orderNumber);
}

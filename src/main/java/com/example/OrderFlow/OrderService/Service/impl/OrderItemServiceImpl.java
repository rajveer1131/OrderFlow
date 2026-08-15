package com.example.OrderFlow.OrderService.Service.impl;

import com.example.OrderFlow.OrderService.Model.OrderItem;
import com.example.OrderFlow.OrderService.Repository.OrderItemRepository;
import com.example.OrderFlow.OrderService.Service.OrderItemService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository orderItemRepository;

    public OrderItemServiceImpl(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public List<OrderItem> getOrderItemsByOrderId(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }
}

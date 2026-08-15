package com.example.OrderFlow.OrderService.Service;

import com.example.OrderFlow.OrderService.Model.OrderItem;
import java.util.List;

public interface OrderItemService {
    List<OrderItem> getOrderItemsByOrderId(Long orderId);
}

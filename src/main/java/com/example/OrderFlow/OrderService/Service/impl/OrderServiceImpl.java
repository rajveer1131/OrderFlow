package com.example.OrderFlow.OrderService.Service.impl;

import com.example.OrderFlow.InventoryService.Models.Product;
import com.example.OrderFlow.InventoryService.Service.ProductService;
import com.example.OrderFlow.OrderService.DTO.Mapper.OrderMapper;
import com.example.OrderFlow.OrderService.DTO.ResponseDTO.OrderResponseDTO;
import com.example.OrderFlow.OrderService.Model.*;
import com.example.OrderFlow.OrderService.Repository.OrderRepository;
import com.example.OrderFlow.OrderService.Service.CartService;
import com.example.OrderFlow.OrderService.Service.OrderService;
import com.example.OrderFlow.PaymentService.DTO.PaymentRequestDTO;
import com.example.OrderFlow.PaymentService.DTO.PaymentResponseDTO;
import com.example.OrderFlow.PaymentService.Model.Payment;
import com.example.OrderFlow.PaymentService.Model.PaymentMode;
import com.example.OrderFlow.PaymentService.Model.PaymentStatus;
import com.example.OrderFlow.PaymentService.Service.PaymentService;
import com.example.OrderFlow.UserService.Model.Address;
import com.example.OrderFlow.UserService.Service.AddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final ProductService productService;
    private final OrderMapper orderMapper;
    private final AddressService addressService;
    private final PaymentService paymentService;

    public OrderServiceImpl(OrderRepository orderRepository, CartService cartService, ProductService productService,OrderMapper orderMapper,AddressService addressService,PaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.productService = productService;
        this.orderMapper = orderMapper;
        this.addressService = addressService;
        this.paymentService = paymentService;
    }

    @Override
    @Transactional
    public OrderResponseDTO checkout(Long userId, Long shippingAddressId) {
        Cart cart = cartService.getCartForCheckOut(userId);

        if(cart.getItems().isEmpty()){
            throw new IllegalArgumentException("Cart is empty");
        }
        Address address = addressService.getAddressByIdForUser(
                shippingAddressId,
                userId
        );
        String orderNumber = generateOrderNumber();

        OrderShippingAddress orderShippingAddress = OrderShippingAddress.builder()
                .city(address.getCity())
                .state(address.getState())
                .street(address.getStreet())
                .country(address.getCountry())
                .zipCode(address.getZipCode())
                .build();

        List<OrderItem> orderItemList = cart.getItems().stream()
                .map(cartItem -> {
                    Product product = productService.getProductEntityById(cartItem.getProduct().getId());
                    if (!product.isActive()) {
                        throw new IllegalArgumentException(
                                "Product is no longer active: " + product.getId()
                        );
                    }

                    if (product.getStockQuantity() < cartItem.getQuantity()) {
                        throw new IllegalArgumentException(
                                "Insufficient stock for product: " + product.getId()
                        );
                    }

                    BigDecimal price = product.getPrice();
                    product.setStockQuantity(product.getStockQuantity()- cartItem.getQuantity());

                    return OrderItem.builder()
                            .productId(product.getId())
                            .productName(product.getName())
                            .sku(product.getSku())
                            .priceAtOrder(price)
                            .quantity(cartItem.getQuantity())
                            .subTotal(
                                    price.multiply(
                                            BigDecimal.valueOf(cartItem.getQuantity())
                                    )
                            )
                            .build();
                }).toList();
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .status(OrderStatus.PENDING)
                .shippingAddress(orderShippingAddress)
                .paymentStatus(PaymentStatus.PENDING)
                .userId(userId)
                .build();

        orderItemList.forEach(order::addItem);

        order.recalculateTotal();



        Order savedOrder = orderRepository.save(order);
        cart.clearItems();

        PaymentRequestDTO paymentRequest = PaymentRequestDTO.builder()
                .orderId(savedOrder.getId())
                .paymentMode(PaymentMode.CARD)
                .paymentAmount(savedOrder.getTotalAmount())
                .simulatedPayment(true)
                .build();

        PaymentResponseDTO paymentResponse =
                paymentService.processPayment(paymentRequest);

        if(paymentResponse.getPaymentStatus().equals(PaymentStatus.FAILED.name())){
            savedOrder.setPaymentStatus(PaymentStatus.FAILED);
            cancelOrder(savedOrder.getId());

        }else{
            savedOrder.setStatus(OrderStatus.CONFIRMED);
            savedOrder.setPaymentStatus(PaymentStatus.SUCCESS);
        }



        return orderMapper.toResponse(savedOrder);
    }

    public String generateOrderNumber(){
            String generatedNumber = "";
            int retry = 5;

            String alphaChars = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
            do{
                StringBuilder number = new StringBuilder("ORD-");
                retry--;

                for(int i=0;i<8;i++){
                    int rand = (int)(Math.random()*alphaChars.length());
                    number.append(alphaChars.charAt(rand));
                }
                number.append("-");

                for(int i=0;i<8;i++){
                    int rand = (int)(Math.random()*alphaChars.length());
                    number.append(alphaChars.charAt(rand));
                }

                generatedNumber = number.toString();
            }while(orderRepository.existsByOrderNumber(generatedNumber) && retry>0);
            if (retry == 0 && orderRepository.existsByOrderNumber(generatedNumber)) {
                throw new IllegalStateException("Unable to generate unique Order number");
            }
            return generatedNumber;
        }


    @Override
    public OrderResponseDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + id));
        return orderMapper.toResponse(order);
    }

    @Override
    public OrderResponseDTO getOrderByOrderNumber(String orderNumber) {

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with order number: " + orderNumber));
        return orderMapper.toResponse(order);
    }

    @Override
    public List<OrderResponseDTO> getOrdersByUserId(Long userId) {
        List<Order> orderList = orderRepository.findByUserId(userId);
        return orderList.stream().map(orderMapper::toResponse).toList();
    }

    @Override
    public List<OrderResponseDTO> getOrdersByStatus(OrderStatus status) {
        List<Order> orderList = orderRepository.findByStatus(status);
        return orderList.stream().map(orderMapper::toResponse).toList();

    }

    @Override
    @Transactional
    public OrderResponseDTO updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));
        validateStatusTransition(order.getStatus(),status);
        order.setStatus(status);
        return orderMapper.toResponse(order);
    }

    private void validateStatusTransition(
            OrderStatus current,
            OrderStatus next
    ) {
        if (next == OrderStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Use cancelOrder() to cancel an order"
            );
        }
        boolean valid = switch (current) {
            case PENDING ->
                    next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED;

            case CONFIRMED ->
                    next == OrderStatus.SHIPPED || next == OrderStatus.CANCELLED;

            case SHIPPED ->
                    next == OrderStatus.DELIVERED;

            case DELIVERED, CANCELLED ->
                    false;
        };

        if (!valid) {
            throw new IllegalStateException(
                    String.format(
                            "Invalid order status transition: %s -> %s",
                            current,
                            next
                    )
            );
        }
    }

    @Override
    @Transactional
    public OrderResponseDTO cancelOrder(Long orderId) {

        Order order = orderRepository.findById(orderId).orElseThrow(
                ()->new IllegalArgumentException("Order does not exists with this id"));
        if(order.getStatus() == OrderStatus.CANCELLED){
            throw new IllegalArgumentException("Order Status is already cancelled");
        }

        if (order.getStatus() != OrderStatus.PENDING &&
                order.getStatus() != OrderStatus.CONFIRMED) {

            throw new IllegalStateException(
                    "Only pending or confirmed orders can be cancelled"
            );
        }

        order.getItems().forEach(orderItem -> {
            productService.restoreStock(orderItem.getProductId(), orderItem.getQuantity());
        });
        order.setStatus(OrderStatus.CANCELLED);

        return orderMapper.toResponse(order);
    }
}

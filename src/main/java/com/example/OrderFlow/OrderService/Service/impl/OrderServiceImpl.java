package com.example.OrderFlow.OrderService.Service.impl;

import com.example.OrderFlow.Common.Exception.InsufficientStockException;
import com.example.OrderFlow.Common.Exception.InvalidOrderStateException;
import com.example.OrderFlow.Common.Exception.ResourceNotFoundException;
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
            throw new InvalidOrderStateException("Cannot checkout with an empty cart");
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
                        throw new ResourceNotFoundException(
                                "Product is no longer active: " + product.getId()
                        );
                    }

                    if (product.getStockQuantity() < cartItem.getQuantity()) {
                        throw new InsufficientStockException(
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
                .simulatedPayment(false)
                .build();

        PaymentResponseDTO paymentResponse =
                paymentService.processPayment(paymentRequest);

        if(paymentResponse.getPaymentStatus().equals(PaymentStatus.FAILED.name())){
            savedOrder.setPaymentStatus(PaymentStatus.FAILED);
            cancelOrder(savedOrder.getId(),userId);

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
                throw new IllegalArgumentException("Unable to generate unique Order number");
            }
            return generatedNumber;
        }


    @Override
    public OrderResponseDTO getOrderByIdForUser(Long id, Long userId) {
        Order order = orderRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found")
                );
        return orderMapper.toResponse(order);
    }

    @Override
    public OrderResponseDTO getOrderByOrderNumberForUser(
            String orderNumber,
            Long userId
    ) {
        Order order = orderRepository
                .findByOrderNumberAndUserId(orderNumber, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found"));

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
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        validateStatusTransition(order.getStatus(),status);
        order.setStatus(status);
        return orderMapper.toResponse(order);
    }

    private void validateStatusTransition(
            OrderStatus current,
            OrderStatus next
    ) {
        if (next == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException(
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
            throw new InvalidOrderStateException(
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
    public OrderResponseDTO cancelOrder(Long orderId,Long userId) {

        Order order = orderRepository
                .findByIdAndUserId(orderId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found")
                );
        if(order.getStatus() == OrderStatus.CANCELLED){
            throw new InvalidOrderStateException("Order Status is already cancelled");
        }

        if (order.getStatus() != OrderStatus.PENDING &&
                order.getStatus() != OrderStatus.CONFIRMED) {

            throw new InvalidOrderStateException(
                    "Only pending or confirmed orders can be cancelled"
            );
        }

        // TODO: Need to implement Refund
//        if(order.getStatus() == OrderStatus.CONFIRMED){
//
//        }
        order.getItems().forEach(orderItem -> {
            productService.restoreStock(orderItem.getProductId(), orderItem.getQuantity());
        });
        order.setStatus(OrderStatus.CANCELLED);


        return orderMapper.toResponse(order);
    }
}

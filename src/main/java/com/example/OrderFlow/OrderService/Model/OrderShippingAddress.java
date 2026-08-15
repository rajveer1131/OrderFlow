package com.example.OrderFlow.OrderService.Model;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderShippingAddress {

    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
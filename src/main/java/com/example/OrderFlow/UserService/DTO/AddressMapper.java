package com.example.OrderFlow.UserService.DTO;

import com.example.OrderFlow.UserService.DTO.RequestDTO.AddressRequestDTO;
import com.example.OrderFlow.UserService.DTO.ResponseDTO.AddressResponseDTO;
import com.example.OrderFlow.UserService.DTO.ResponseDTO.UserAddressDTO;
import com.example.OrderFlow.UserService.Model.Address;
import com.example.OrderFlow.UserService.Model.User;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public AddressResponseDTO toResponse(Address address) {
        return AddressResponseDTO.builder()
                .id(address.getId())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .zipCode(address.getZipCode())
                .country(address.getCountry())
                .build();
    }

    public Address toEntity(AddressRequestDTO dto, User user){
        return Address.builder()
                .street(dto.getStreet())
                .city(dto.getCity())
                .state(dto.getState())
                .zipCode(dto.getZipCode())
                .country(dto.getCountry())
                .user(user)
                .build();

    }
}

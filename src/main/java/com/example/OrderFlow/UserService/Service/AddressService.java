package com.example.OrderFlow.UserService.Service;

import com.example.OrderFlow.UserService.DTO.RequestDTO.AddressRequestDTO;
import com.example.OrderFlow.UserService.DTO.ResponseDTO.AddressResponseDTO;
import com.example.OrderFlow.UserService.Model.Address;
import org.springframework.stereotype.Service;

@Service
public interface AddressService {

    public AddressResponseDTO createAddress(AddressRequestDTO addressRequestDTO ,Long id);

    public AddressResponseDTO updateAddress(AddressRequestDTO addressRequestDTO, Long addressId,Long userId);

    public void deleteAddress(Long id,Long userId);

    public Address getAddressByIdForUser(Long addressId,Long userId);
}

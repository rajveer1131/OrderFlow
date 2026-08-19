package com.example.OrderFlow.UserService.Service.impl;

import com.example.OrderFlow.Common.Exception.ResourceNotFoundException;
import com.example.OrderFlow.UserService.DTO.AddressMapper;
import com.example.OrderFlow.UserService.DTO.RequestDTO.AddressRequestDTO;
import com.example.OrderFlow.UserService.DTO.ResponseDTO.AddressResponseDTO;
import com.example.OrderFlow.UserService.Model.Address;
import com.example.OrderFlow.UserService.Model.User;
import com.example.OrderFlow.UserService.Repository.AddressRepository;
import com.example.OrderFlow.UserService.Repository.UserRepository;
import com.example.OrderFlow.UserService.Service.AddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AddressServiceImpl implements AddressService {

    private final UserRepository userRepository;
    private final AddressMapper addressMapper;
    private final AddressRepository addressRepository;

    public AddressServiceImpl(UserRepository userRepository,AddressMapper addressMapper,AddressRepository addressRepository){
        this.userRepository = userRepository;
        this.addressMapper = addressMapper;
        this.addressRepository = addressRepository;
    }


    @Override
    public AddressResponseDTO createAddress(AddressRequestDTO addressRequestDTO ,Long id) {
        User user = userRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("User does not exists with id: "+id));
        Address address = addressMapper.toEntity(addressRequestDTO,user);

        return addressMapper.toResponse(addressRepository.save(address));
    }

    @Override
    public AddressResponseDTO updateAddress(AddressRequestDTO addressRequestDTO, Long addressId,Long userId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address does not exists with id: "+addressId));

        if(!address.getUser().getId().equals(userId)){
            throw  new IllegalArgumentException("Not authorized to modify this address");
        }

        if (addressRequestDTO.getStreet() != null) {
            address.setStreet(addressRequestDTO.getStreet());
        }
        if (addressRequestDTO.getCity() != null) {
            address.setCity(addressRequestDTO.getCity());
        }
        if (addressRequestDTO.getState() != null) {
            address.setState(addressRequestDTO.getState());
        }
        if (addressRequestDTO.getZipCode() != null) {
            address.setZipCode(addressRequestDTO.getZipCode());
        }
        if (addressRequestDTO.getCountry() != null) {
            address.setCountry(addressRequestDTO.getCountry());
        }

        return addressMapper.toResponse(address);
    }
    @Override
    public void deleteAddress(Long id) {

        if(!addressRepository.existsById(id)){
            throw new ResourceNotFoundException("Address does not exists with id: "+id);
        }
        addressRepository.deleteById(id);

    }

    @Override
    public Address getAddressByIdForUser(Long addressId, Long userId) {

        return addressRepository.findByIdAndUserId(addressId,userId).orElseThrow(()-> new ResourceNotFoundException("Address  does not exists with userId: "+userId));
    }
}

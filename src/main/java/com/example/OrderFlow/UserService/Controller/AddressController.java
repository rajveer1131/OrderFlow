package com.example.OrderFlow.UserService.Controller;

import com.example.OrderFlow.Config.Security.CustomUserDetails;
import com.example.OrderFlow.UserService.DTO.RequestDTO.AddressRequestDTO;
import com.example.OrderFlow.UserService.DTO.ResponseDTO.AddressResponseDTO;
import com.example.OrderFlow.UserService.Service.AddressService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/address")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService){
        this.addressService = addressService;
    }

    @PostMapping()
    public ResponseEntity<?> createAddress(@RequestBody AddressRequestDTO addressRequestDTO, @AuthenticationPrincipal CustomUserDetails userDetails){
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.createAddress(addressRequestDTO,userDetails.getUserId()));

    }

    @PutMapping("/{addressId}")
    public ResponseEntity<?> updateAddress(
            @RequestBody AddressRequestDTO addressRequestDTO,
            @PathVariable Long addressId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        return ResponseEntity.ok(addressService.updateAddress(addressRequestDTO, addressId, userDetails.getUserId()));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<?> deleteAddress(
            @PathVariable Long addressId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        addressService.deleteAddress(
                addressId,
                userDetails.getUserId()
        );

        return ResponseEntity.ok("Address Deleted Successfully");
    }
}

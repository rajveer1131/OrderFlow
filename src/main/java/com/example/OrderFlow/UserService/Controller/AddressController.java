package com.example.OrderFlow.UserService.Controller;

import com.example.OrderFlow.UserService.DTO.RequestDTO.AddressRequestDTO;
import com.example.OrderFlow.UserService.DTO.ResponseDTO.AddressResponseDTO;
import com.example.OrderFlow.UserService.Service.AddressService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/address")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService){
        this.addressService = addressService;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<?> createAddress(@RequestBody AddressRequestDTO addressRequestDTO, @PathVariable Long userId){
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.createAddress(addressRequestDTO,userId));

    }

    @PutMapping("/{addressId}")
    public ResponseEntity<?> updateAddress(
            @RequestBody AddressRequestDTO addressRequestDTO,
            @PathVariable Long addressId,
            @RequestParam Long userId // TODO: Ownership check with id but will update with Auth later
    ){
        return ResponseEntity.ok(addressService.updateAddress(addressRequestDTO, addressId, userId));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<?> deleteAddress(@PathVariable Long addressId){
        addressService.deleteAddress(addressId);
        return ResponseEntity.ok("Address Deleted Successfully");
    }
}

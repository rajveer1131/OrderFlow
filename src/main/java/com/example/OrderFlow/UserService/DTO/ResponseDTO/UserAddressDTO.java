package com.example.OrderFlow.UserService.DTO.ResponseDTO;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserAddressDTO {


    private Long id;
    private Long UserId;

    private List<AddressResponseDTO> userAddressList;
}

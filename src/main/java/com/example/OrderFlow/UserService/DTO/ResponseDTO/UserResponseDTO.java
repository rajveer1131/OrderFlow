package com.example.OrderFlow.UserService.DTO.ResponseDTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDTO {

    private Long id;

    private String name;

    private String email;
}

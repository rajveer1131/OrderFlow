package com.example.OrderFlow.UserService.DTO;

import com.example.OrderFlow.UserService.DTO.RequestDTO.UserRegisterDTO;
import com.example.OrderFlow.UserService.DTO.ResponseDTO.AuthResponse;
import com.example.OrderFlow.UserService.DTO.ResponseDTO.UserResponseDTO;
import com.example.OrderFlow.UserService.Model.User;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {
    public AuthResponse toResponse(User user, String token){
        return AuthResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .token(token)
                .build();
    }

    public User toEntity(UserRegisterDTO userRegisterDTO){
        User user = new User();
        user.setName(userRegisterDTO.getName());
        user.setEmail(userRegisterDTO.getEmail());
        user.setPassword(userRegisterDTO.getPassword());
        return user;
    }
}

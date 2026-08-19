package com.example.OrderFlow.UserService.Service;

import com.example.OrderFlow.UserService.DTO.RequestDTO.UserLoginDTO;
import com.example.OrderFlow.UserService.DTO.RequestDTO.UserRegisterDTO;
import com.example.OrderFlow.UserService.DTO.ResponseDTO.AuthResponse;
import com.example.OrderFlow.UserService.DTO.ResponseDTO.UserResponseDTO;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {
    public AuthResponse userCreate(UserRegisterDTO userRegisterDTO);
    public AuthResponse userLogin(UserLoginDTO userLoginDTO);
}

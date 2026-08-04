package com.example.OrderFlow.UserService.Service;

import com.example.OrderFlow.UserService.DTO.RequestDTO.UserLoginDTO;
import com.example.OrderFlow.UserService.DTO.RequestDTO.UserRegisterDTO;
import com.example.OrderFlow.UserService.DTO.ResponseDTO.UserResponseDTO;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

    public UserResponseDTO UserCreate(UserRegisterDTO userRegisterDTO);
    public UserResponseDTO userLogin(UserLoginDTO userLoginDTO);
    public void userDelete(Long id);
    public UserResponseDTO userUpdate(UserRegisterDTO userRegisterDTO, Long id);
}

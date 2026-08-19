package com.example.OrderFlow.UserService.Service.impl;

import com.example.OrderFlow.Common.Exception.DuplicateResourceException;
import com.example.OrderFlow.Common.Exception.ResourceNotFoundException;
import com.example.OrderFlow.Config.Security.JwtUtils;
import com.example.OrderFlow.UserService.DTO.RequestDTO.UserLoginDTO;
import com.example.OrderFlow.UserService.DTO.RequestDTO.UserRegisterDTO;
import com.example.OrderFlow.UserService.DTO.ResponseDTO.UserResponseDTO;
import com.example.OrderFlow.UserService.DTO.UserMapper;
import com.example.OrderFlow.UserService.Model.User;
import com.example.OrderFlow.UserService.Repository.UserRepository;
import com.example.OrderFlow.UserService.Service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper){
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }


    @Override
    public void userDelete(Long id) {

        //TODO: Need to handle User Address delete also
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User does not exist with id: "+id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserResponseDTO userUpdate(UserRegisterDTO userRegisterDTO, Long id) {
        User user = userRepository.findById(id).orElseThrow(
                ()-> new ResourceNotFoundException("User does not exist with id: "+id));
        if(userRegisterDTO.getName()!=null){
            user.setName(userRegisterDTO.getName());
        }
        if (userRegisterDTO.getEmail() != null && !userRegisterDTO.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userRegisterDTO.getEmail())) {
                throw new DuplicateResourceException("Email already in use");
            }
            user.setEmail(userRegisterDTO.getEmail());
        }

        return userMapper.toResponse(user);
    }
}

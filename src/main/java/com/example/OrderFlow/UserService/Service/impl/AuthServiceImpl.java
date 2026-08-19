package com.example.OrderFlow.UserService.Service.impl;

import com.example.OrderFlow.Common.Exception.DuplicateResourceException;
import com.example.OrderFlow.Common.Exception.ResourceNotFoundException;
import com.example.OrderFlow.Config.Security.JwtUtils;
import com.example.OrderFlow.UserService.DTO.AuthMapper;
import com.example.OrderFlow.UserService.DTO.RequestDTO.UserLoginDTO;
import com.example.OrderFlow.UserService.DTO.RequestDTO.UserRegisterDTO;
import com.example.OrderFlow.UserService.DTO.ResponseDTO.AuthResponse;
import com.example.OrderFlow.UserService.DTO.ResponseDTO.UserResponseDTO;
import com.example.OrderFlow.UserService.DTO.UserMapper;
import com.example.OrderFlow.UserService.Model.User;
import com.example.OrderFlow.UserService.Repository.UserRepository;
import com.example.OrderFlow.UserService.Service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(UserRepository userRepository, AuthMapper authMapper, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, AuthenticationManager authenticationManager){
        this.userRepository = userRepository;
        this.authMapper = authMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
    }


    @Override
    public AuthResponse userCreate(UserRegisterDTO userRegisterDTO) {
        if(userRepository.existsByEmail(userRegisterDTO.getEmail())){
            throw new DuplicateResourceException("User Already Exists");
        }
        User user = authMapper.toEntity(userRegisterDTO);

        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);
        User saveduser = userRepository.save(user);
        return userLogin(new UserLoginDTO(saveduser.getEmail(), userRegisterDTO.getPassword()));

    }

    @Override
    public AuthResponse userLogin(UserLoginDTO dto) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(),dto.getPassword())
        );
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String token = jwtUtils.generateToken(dto.getEmail());

        return authMapper.toResponse(user,token);
    }
}

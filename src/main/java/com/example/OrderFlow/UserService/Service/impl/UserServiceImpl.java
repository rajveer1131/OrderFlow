package com.example.OrderFlow.UserService.Service.impl;

import com.example.OrderFlow.UserService.DTO.RequestDTO.UserLoginDTO;
import com.example.OrderFlow.UserService.DTO.RequestDTO.UserRegisterDTO;
import com.example.OrderFlow.UserService.DTO.ResponseDTO.UserResponseDTO;
import com.example.OrderFlow.UserService.DTO.UserMapper;
import com.example.OrderFlow.UserService.Model.User;
import com.example.OrderFlow.UserService.Repository.UserRepository;
import com.example.OrderFlow.UserService.Service.UserService;
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
    public UserResponseDTO UserCreate(UserRegisterDTO userRegisterDTO) {
        if(userRepository.existsByEmail(userRegisterDTO.getEmail())){
            throw new IllegalArgumentException("User Already Exists");
        }
        User user = userMapper.toEntity(userRegisterDTO);

        // TODO Will add encoder After Flow Works
//        String hashedPass = userRequestDTO.getPassword();
//        user.setPassword(hashedPass);
        return userMapper.toResponse(userRepository.save(user));

    }

    @Override
    public UserResponseDTO userLogin(UserLoginDTO userLoginDTO) {

        User user = userRepository.findByEmail(userLoginDTO.getEmail()).orElseThrow(
                ()-> new IllegalArgumentException("Invalid Credentials"));

        if(!userLoginDTO.getPassword().equals(user.getPassword())){
            throw  new IllegalArgumentException("Invalid Credentials");
        }

        return userMapper.toResponse(user);
    }

    @Override
    public void userDelete(Long id) {

        //TODO: Need to handle User Address delete also
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("UserId does not exist");
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserResponseDTO userUpdate(UserRegisterDTO userRegisterDTO, Long id) {
        User user = userRepository.findById(id).orElseThrow(
                ()-> new IllegalArgumentException("UserId Does not exist"));
        if(userRegisterDTO.getName()!=null){
            user.setName(userRegisterDTO.getName());
        }
        if (userRegisterDTO.getEmail() != null && !userRegisterDTO.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userRegisterDTO.getEmail())) {
                throw new IllegalArgumentException("Email already in use");
            }
            user.setEmail(userRegisterDTO.getEmail());
        }

        return userMapper.toResponse(user);
    }
}

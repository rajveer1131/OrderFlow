package com.example.OrderFlow.UserService.Controller;

import com.example.OrderFlow.UserService.DTO.RequestDTO.UserLoginDTO;
import com.example.OrderFlow.UserService.DTO.RequestDTO.UserRegisterDTO;
import com.example.OrderFlow.UserService.Service.AuthService;
import com.example.OrderFlow.UserService.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> userLogin(@Valid @RequestBody UserLoginDTO userLoginDTO){
        return ResponseEntity.ok(authService.userLogin(userLoginDTO));
    }

    @PostMapping("/signup")
    public  ResponseEntity<?> userSignup(@Valid @RequestBody UserRegisterDTO userRegisterDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.userCreate(userRegisterDTO));
    }

}

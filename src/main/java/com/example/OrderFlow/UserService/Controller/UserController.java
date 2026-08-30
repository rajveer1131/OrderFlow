package com.example.OrderFlow.UserService.Controller;

import com.example.OrderFlow.Config.Security.CustomUserDetails;
import com.example.OrderFlow.UserService.DTO.RequestDTO.UserLoginDTO;
import com.example.OrderFlow.UserService.DTO.RequestDTO.UserRegisterDTO;
import com.example.OrderFlow.UserService.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> userDelete(@AuthenticationPrincipal CustomUserDetails userDetails){
        userService.userDelete(userDetails.getUserId());
        return ResponseEntity.ok("User Delete successfully");
    }

    @PutMapping("/update")
    public ResponseEntity<?> userUpdate(@Valid @RequestBody UserRegisterDTO userRegisterDTO,@AuthenticationPrincipal CustomUserDetails userDetails){
        return ResponseEntity.ok(userService.userUpdate(userRegisterDTO, userDetails.getUserId()));
    }
}

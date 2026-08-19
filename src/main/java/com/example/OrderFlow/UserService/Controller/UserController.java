package com.example.OrderFlow.UserService.Controller;

import com.example.OrderFlow.UserService.DTO.RequestDTO.UserLoginDTO;
import com.example.OrderFlow.UserService.DTO.RequestDTO.UserRegisterDTO;
import com.example.OrderFlow.UserService.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> userDelete(@PathVariable Long id){
        userService.userDelete(id);
        return ResponseEntity.ok("User Delete successfully");
    }

    @PutMapping("{id}")
    public ResponseEntity<?> userUpdate(@Valid @RequestBody UserRegisterDTO userRegisterDTO,@PathVariable Long id){
        return ResponseEntity.ok(userService.userUpdate(userRegisterDTO,id));
    }
}

package com.example.OrderFlow.UserService.DTO.RequestDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginDTO {
    @Email
    private String email;
    @Size(min = 6,message = "Password should be at least 6 character long")
    private String password;
}

package com.example.OrderFlow.UserService.DTO.RequestDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class UserRegisterDTO {

    @NotBlank(message = "Name should not be blank")
    private String name;
    @Email
    private String email;
    @Size(min = 6,message = "Password should be at least 6 character long")
    private String password;
}

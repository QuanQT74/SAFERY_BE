package com.example.banckend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    @NotBlank(message = "Phone number must not be blank")
    private String phoneNumber;

    @NotBlank(message = "Password must not be blank")
    private String password;
}

package com.example.banckend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ForgotPasswordRequest {
    @NotBlank(message = "Phone number must not be blank")
    private String phoneNumber;
}

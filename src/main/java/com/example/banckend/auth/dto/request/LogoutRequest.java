package com.example.banckend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter

public class LogoutRequest {
    @NotBlank(message = "Refresh token required")
    private String refreshToken;
}

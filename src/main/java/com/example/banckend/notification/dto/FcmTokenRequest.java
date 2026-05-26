package com.example.banckend.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FcmTokenRequest {
    @NotBlank(message = "FCM token is required")
    private String fcmToken;
}

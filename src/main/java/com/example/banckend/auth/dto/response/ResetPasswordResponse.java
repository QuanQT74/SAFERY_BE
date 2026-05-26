package com.example.banckend.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResetPasswordResponse {
    private String message;
    private String phoneNumber;
}

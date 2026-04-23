package com.example.banckend.auth.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ForgotPasswordResponse {
    private String message;
    private String phoneNumber;
}

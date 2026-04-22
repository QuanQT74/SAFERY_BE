package com.example.banckend.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {

    private String message;
    private String phoneNumber;
    private String fullName;
    private Boolean phoneVerified;

    // TODO: Remove this field after SMS integration - only for development/testing
    private String otpCode;
}

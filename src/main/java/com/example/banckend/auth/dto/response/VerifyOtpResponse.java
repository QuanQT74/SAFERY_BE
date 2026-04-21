package com.example.banckend.auth.dto.response;

import com.example.banckend.conmon.enums.OtpPurpose;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VerifyOtpResponse {

    private String message;

    private String phoneNumber;

    private OtpPurpose purpose;
    
    private Boolean phoneVerified;
}

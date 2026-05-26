package com.example.banckend.auth.dto.response;

import com.example.banckend.conmon.enums.OtpPurpose;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerifyOtpResponse {

    private String message;

    private String phoneNumber;

    private OtpPurpose purpose;

    private Boolean phoneVerified;

    private String resetToken;
}

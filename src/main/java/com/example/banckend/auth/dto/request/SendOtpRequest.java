package com.example.banckend.auth.dto.request;

import com.example.banckend.conmon.enums.OtpPurpose;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendOtpRequest {
    @NotBlank(message = "Phone number must not be blank")
    private String phoneNumber;

    private OtpPurpose purpose;
}

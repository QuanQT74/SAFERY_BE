package com.example.banckend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {
    @NotBlank(message = "Phone number must not be blank")
    private String phoneNumber;

    @NotBlank(message = "Reset token must not be blank")
    private String resetToken;

    @NotBlank(message = "New password must not be blank")
    private String newPassword;

    @NotBlank(message = "Confirm password must not be blank")
    private String confirmPassword;
}

package com.example.banckend.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {
    @Schema(description = "The user's current password", example = "oldPassword123")
    @NotBlank(message = "Old password is required")
    private String oldPassword;

    @Schema(description = "The new password for the user", example = "newStrongPassword!@#")
    @NotBlank(message = "New password is required")
    @Length(min = 6, message = "Password must be at least 6 characters long")
    private String newPassword;

    @Schema(description = "Confirmation of the new password", example = "newStrongPassword!@#")
    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;
}

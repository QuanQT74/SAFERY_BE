package com.example.banckend.auth.controller;

import com.example.banckend.auth.dto.request.*;
import com.example.banckend.auth.dto.response.*;
import com.example.banckend.auth.service.AuthService;
import com.example.banckend.auth.service.RefreshTokenService;
import com.example.banckend.conmon.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "APIs for user authentication")
@RequiredArgsConstructor
public class AuthController {

        private final AuthService authService;
        private final RefreshTokenService refreshTokenService;

        // ==================== LOGIN ====================
        @PostMapping("/login")
        public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
                LoginResponse response = authService.login(request);
                return ResponseEntity.ok(ApiResponse.success("Login successful", response));
        }

        // ==================== REGISTER ====================
        @PostMapping("/register")
        public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
                RegisterResponse response = authService.register(request);
                return ResponseEntity.ok(ApiResponse.success("Registration initiated. Please verify OTP.", response));
        }

        // ==================== OTP (Gộp chung cho cả Register & Forgot Password) ====================
        @PostMapping("/send-otp")
        public ResponseEntity<ApiResponse<SendOtpResponse>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
                SendOtpResponse response = authService.sendOtp(request);
                return ResponseEntity.ok(ApiResponse.success("OTP sent successfully", response));
        }

        @PostMapping("/verify-otp")
        public ResponseEntity<ApiResponse<VerifyOtpResponse>> verifyOtp(
                        @Valid @RequestBody VerifyOtpRequest request) {
                VerifyOtpResponse response = authService.verifyOtp(request);
                return ResponseEntity.ok(ApiResponse.success("OTP verified successfully", response));
        }

        // ==================== FORGOT PASSWORD ====================
        @PostMapping("/reset-password")
        public ResponseEntity<ApiResponse<ResetPasswordResponse>> resetPassword(
                        @Valid @RequestBody ResetPasswordRequest request) {
                ResetPasswordResponse response = authService.resetPassword(request);
                return ResponseEntity.ok(ApiResponse.success("Password reset successful", response));
        }

        // ==================== CHANGE PASSWORD (Authenticated) ====================
        @PostMapping("/change-password")
        public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
                authService.changePassword(request);
                return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
        }

        // ==================== TOKEN MANAGEMENT ====================
        @PostMapping("/refresh-token")
        public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshToken(
                        @Valid @RequestBody TokenRefreshRequest request) {
                String newAccessToken = refreshTokenService.generateNewAccessToken(
                                refreshTokenService.findByToken(request.getRefreshToken())
                                                .map(refreshTokenService::verifyExpiration)
                                                .orElseThrow(() -> new com.example.banckend.conmon.exception.BadRequestException("Refresh token not found!")));
                TokenRefreshResponse response = TokenRefreshResponse.of(
                                newAccessToken,
                                request.getRefreshToken(),
                                refreshTokenService.getAccessTokenExpiration());
                return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
        }

        @PostMapping("/logout")
        public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequest request) {
                authService.logout(request);
                return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
        }
}

package com.example.banckend.auth.controller;

import com.example.banckend.auth.dto.request.LoginRequest;
import com.example.banckend.auth.dto.request.RegisterRequest;
import com.example.banckend.auth.dto.request.VerifyOtpRequest;
import com.example.banckend.auth.dto.response.LoginResponse;
import com.example.banckend.auth.dto.response.RegisterResponse;
import com.example.banckend.auth.dto.response.VerifyOtpResponse;
import com.example.banckend.auth.service.AuthService;
import com.example.banckend.conmon.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<VerifyOtpResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        VerifyOtpResponse response = authService.verifyOtp(request);
        return ResponseEntity.ok(
             ApiResponse.<VerifyOtpResponse>builder()
                        .message("Verify OTP successful")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(new ApiResponse<>(response));
    }
}

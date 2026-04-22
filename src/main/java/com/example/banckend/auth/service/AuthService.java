package com.example.banckend.auth.service;

import com.example.banckend.auth.dto.request.LoginRequest;
import com.example.banckend.auth.dto.request.RegisterRequest;
import com.example.banckend.auth.dto.request.VerifyOtpRequest;
import com.example.banckend.auth.dto.response.LoginResponse;
import com.example.banckend.auth.dto.response.RegisterResponse;
import com.example.banckend.auth.dto.response.UserSummaryResponse;
import com.example.banckend.auth.dto.response.VerifyOtpResponse;
import com.example.banckend.auth.entity.OtpVerification;
import com.example.banckend.auth.entity.User;
import com.example.banckend.auth.repository.OtpVerificationRepository;
import com.example.banckend.auth.repository.UserRepository;
import com.example.banckend.conmon.enums.OtpPurpose;
import com.example.banckend.conmon.enums.UserStatus;
import com.example.banckend.conmon.exception.CustomException;
import com.example.banckend.conmon.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final OtpVerificationRepository otpVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_VALIDITY_MINUTES = 5;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        // Validate password match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new CustomException(ErrorCode.CONFIRM_PASSWORD_NOT_MATCH);
        }

        // Check if phone number already exists
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new CustomException(ErrorCode.PHONE_NUMBER_ALREADY_EXISTS, request.getPhoneNumber());
        }

        // Create new user
        User user = User.builder()
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phoneVerified(false)
                .status(UserStatus.ACTIVE)
                .build();

        // Generate OTP
        String otpCode = generateOtpCode();
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES);

        OtpVerification otpVerification = OtpVerification.builder()
                .phoneNumber(request.getPhoneNumber())
                .otpCode(otpCode)
                .purpose(OtpPurpose.REGISTER)
                .expiredAt(expiredAt)
                .verified(false)
                .build();

        // Save user and OTP
        userRepository.save(user);
        otpVerificationRepository.save(otpVerification);

        // TODO: Remove this log after SMS integration - only for development/testing
        log.info("=== DEV MODE: OTP for phone {} is: {} ===", request.getPhoneNumber(), otpCode);

        return RegisterResponse.builder()
                .message("Registration successful. Please verify your phone number.")
                .phoneNumber(user.getPhoneNumber())
                .fullName(user.getFullName())
                .phoneVerified(user.getPhoneVerified())
                .otpCode(otpCode) // TODO: Remove this field after SMS integration
                .build();
    }

    @Transactional
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
        // Find latest OTP for phone number with given purpose and not verified
        Optional<OtpVerification> otpOpt = otpVerificationRepository
                .findTopByPhoneNumberAndPurposeOrderByCreatedAtDesc(
                        request.getPhoneNumber(),
                        request.getPurpose());

        if (otpOpt.isEmpty()) {
            throw new CustomException(ErrorCode.RECORD_NOT_FOUND, "OTP not found or already verified");
        }

        OtpVerification otp = otpOpt.get();

        // Check if OTP is expired
        if (otp.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.TOKEN_EXPIRED, "OTP has expired");
        }

        // Check if OTP code matches
        if (!otp.getOtpCode().equals(request.getOtpCode())) {
            throw new CustomException(ErrorCode.TOKEN_INVALID, "Invalid OTP code");
        }

        // Mark OTP as verified
        otp.setVerified(true);
        otpVerificationRepository.save(otp);

        // If purpose is SIGN_UP, mark user's phone as verified
        if (request.getPurpose() == OtpPurpose.REGISTER) {
            User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, request.getPhoneNumber()));
            user.setPhoneVerified(true);
            userRepository.save(user);
        }

        return VerifyOtpResponse.builder()
                .message("OTP verification successful")
                .phoneNumber(request.getPhoneNumber())
                .purpose(request.getPurpose())
                .phoneVerified(true)
                .build();
    }
    /// CHức  năng đăng nhập
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // Find user by phone number
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        // Check account status
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new CustomException(ErrorCode.ACCOUNT_LOCKED);
        }
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new CustomException(ErrorCode.ACCOUNT_INACTIVE);
        }

        // Check phone verification if required
        if (!user.getPhoneVerified()) {
            throw new CustomException(ErrorCode.PHONE_NOT_VERIFIED);
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Build user summary
        UserSummaryResponse userSummary = UserSummaryResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .phoneVerified(user.getPhoneVerified())
                .build();

        // Build login response
        LoginResponse response = LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .user(userSummary)
                .build();

        return response;
    }

    private String generateOtpCode() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
}

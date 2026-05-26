package com.example.banckend.auth.service;

import com.example.banckend.auth.dto.request.SendOtpRequest;
import com.example.banckend.auth.dto.request.ChangePasswordRequest;
import com.example.banckend.auth.dto.request.LoginRequest;
import com.example.banckend.auth.dto.request.LogoutRequest;
import com.example.banckend.auth.dto.request.RegisterRequest;
import com.example.banckend.auth.dto.request.ResetPasswordRequest;
import com.example.banckend.auth.dto.request.VerifyOtpRequest;
import com.example.banckend.auth.dto.response.LoginResponse;
import com.example.banckend.auth.dto.response.RegisterResponse;
import com.example.banckend.auth.dto.response.ResetPasswordResponse;
import com.example.banckend.auth.dto.response.SendOtpResponse;
import com.example.banckend.auth.dto.response.VerifyOtpResponse;
import com.example.banckend.auth.entity.OtpVerification;
import com.example.banckend.auth.entity.RefreshToken;
import com.example.banckend.auth.entity.User;
import com.example.banckend.auth.repository.OtpVerificationRepository;
import com.example.banckend.auth.repository.RefreshTokenRepository;
import com.example.banckend.auth.repository.UserRepository;
import com.example.banckend.conmon.enums.OtpPurpose;
import com.example.banckend.conmon.enums.UserStatus;
import com.example.banckend.conmon.exception.BadRequestException;
import com.example.banckend.conmon.exception.CustomException;
import com.example.banckend.conmon.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final OtpVerificationRepository otpVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AccountLockService accountLockService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_VALIDITY_MINUTES = 5;

    /**
     * Bước 1: User nhập thông tin -> kiểm tra SĐT -> lưu tạm (KHÔNG gửi OTP)
     * OTP sẽ được gửi qua /send-register-otp
     */
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        // 1. Kiểm tra mật khẩu khớp
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new CustomException(ErrorCode.CONFIRM_PASSWORD_NOT_MATCH);
        }

        // 2. Kiểm tra SĐT đã tồn tại chưa
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new CustomException(ErrorCode.PHONE_NUMBER_ALREADY_EXISTS, request.getPhoneNumber());
        }

        // 3. Lưu tạm thông tin đăng ký (KHÔNG tạo OTP, KHÔNG gửi OTP)
        String passwordHash = passwordEncoder.encode(request.getPassword());

        OtpVerification otpVerification = OtpVerification.builder()
                .phoneNumber(request.getPhoneNumber())
                .otpCode("PENDING") // Chưa có OTP, sẽ được tạo khi gọi /send-register-otp
                .purpose(OtpPurpose.REGISTER)
                .expiredAt(LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES))
                .verified(false)
                .fullName(request.getFullName())
                .passwordHash(passwordHash)
                .build();

        otpVerificationRepository.save(otpVerification);

        log.info("Registration info saved for {}. Please call /send-register-otp to get OTP.", request.getPhoneNumber());

        return RegisterResponse.builder()
                .message("Registration info saved. Please request OTP to verify your phone number.")
                .phoneNumber(request.getPhoneNumber())
                .fullName(request.getFullName())
                .phoneVerified(false)
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

        // Đánh dấu OTP đã xác minh
        otp.setVerified(true);
        String resetToken = null;
        String message = "OTP verification successful";

        if (request.getPurpose() == OtpPurpose.REGISTER) {
            // Tạo User từ thông tin đã lưu tạm trong OtpVerification
            if (otp.getFullName() == null || otp.getPasswordHash() == null) {
                throw new CustomException(ErrorCode.TOKEN_INVALID, "Registration data not found");
            }

            User user = User.builder()
                    .fullName(otp.getFullName())
                    .phoneNumber(otp.getPhoneNumber())
                    .passwordHash(otp.getPasswordHash())
                    .phoneVerified(true)
                    .status(UserStatus.ACTIVE)
                    .failedLoginAttempts(0)
                    .build();

            userRepository.save(user);

            // Xóa dữ liệu nhạy cảm sau khi tạo User
            otp.setFullName(null);
            otp.setPasswordHash(null);

            message = "Registration successful. You can now login.";
            log.info("User {} registered successfully via OTP verification", user.getPhoneNumber());
        } else if (request.getPurpose() == OtpPurpose.FORGOT_PASSWORD) {
            // Chỉ luồng quên mật khẩu mới tạo reset token
            resetToken = UUID.randomUUID().toString();
            otp.setResetToken(resetToken);
        }

        otpVerificationRepository.save(otp);

        return VerifyOtpResponse.builder()
                .message(message)
                .phoneNumber(request.getPhoneNumber())
                .purpose(request.getPurpose())
                .phoneVerified(true)
                .resetToken(resetToken)
                .build();
    }

    /// CHức năng đăng nhập

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        if (accountLockService.isAccountLocked(user.getId())) {
            throw new CustomException(ErrorCode.ACCOUNT_TEMPORARILY_LOCKED);
        }

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new CustomException(ErrorCode.ACCOUNT_LOCKED);
        }
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new CustomException(ErrorCode.ACCOUNT_INACTIVE);
        }

        if (!user.getPhoneVerified()) {
            throw new CustomException(ErrorCode.PHONE_NOT_VERIFIED);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            accountLockService.incrementFailedAttempts(user.getId());

            if (accountLockService.isMaxAttemptsReached(user.getId())) {
                accountLockService.lockAccount(user.getId());
                log.warn("Account locked due to too many failed login attempts for phone: {}",
                        request.getPhoneNumber());
                throw new CustomException(ErrorCode.TOO_MANY_LOGIN_ATTEMPTS);
            }

            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        accountLockService.resetFailedAttempts(user.getId());

        refreshTokenService.revokeAllRefreshTokensOfUser(user);

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        long accessTokenExpiresIn = (jwtService.extractExpiration(accessToken).getTime() - System.currentTimeMillis())
                / 1000;
        long refreshTokenExpiresIn = (refreshToken.getExpiredAt().atZone(ZoneId.systemDefault()).toInstant()
                .toEpochMilli() - System.currentTimeMillis()) / 1000;

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .accessTokenExpiresIn(accessTokenExpiresIn)
                .refreshTokenExpiresIn(refreshTokenExpiresIn)
                .build();
    }

    //// CHức năng đăng xuất - thu hồi refresh token + xóa FCM token
    @Transactional
    public void logout(LogoutRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BadRequestException("Refresh token is invalid"));

        if (Boolean.TRUE.equals(refreshToken.getRevoked())) {
            throw new BadRequestException("Refresh token has already been revoked");
        }

        if (refreshToken.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Refresh token has expired");
        }

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        // Xóa FCM token khi logout → user không nhận push notification nữa
        User user = refreshToken.getUser();
        if (user != null && user.getFcmToken() != null) {
            user.setFcmToken(null);
            userRepository.save(user);
            log.info("[LOGOUT] Cleared FCM token for userId={}", user.getId());
        }
    }

    private void revokeAllRefreshTokensOfUser(User user) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUserAndRevokedFalse(user);

        for (RefreshToken token : tokens) {
            token.setRevoked(true);
        }

        refreshTokenRepository.saveAll(tokens);
    }

    @Transactional
    public SendOtpResponse sendOtp(SendOtpRequest request) {
        String phoneNumber = request.getPhoneNumber();
        OtpPurpose purpose = request.getPurpose();

        // 1. Kiểm tra thời gian chờ 60 giây (Resend Throttling)
        Optional<OtpVerification> lastOtpOpt = otpVerificationRepository
                .findTopByPhoneNumberAndPurposeOrderByCreatedAtDesc(phoneNumber, purpose);

        if (lastOtpOpt.isPresent()) {
            OtpVerification lastOtp = lastOtpOpt.get();
            // Nếu otpCode là "PENDING" thì đây là lần đầu gửi OTP cho record vừa tạo từ /register
            // -> Cho phép qua luôn, không chặn 60s.
            if (!"PENDING".equals(lastOtp.getOtpCode())) {
                LocalDateTime lastCreatedAt = lastOtp.getCreatedAt();
                long secondsElapsed = java.time.Duration.between(lastCreatedAt, LocalDateTime.now()).getSeconds();
                if (secondsElapsed < 60) {
                    long secondsToWait = 60 - secondsElapsed;
                    throw new CustomException(ErrorCode.RATE_LIMIT_EXCEEDED,
                            "Vui lòng chờ " + secondsToWait + " giây để gửi lại OTP.");
                }
            }
        }

        // 2. Logic tạo/cập nhật OTP
        String otpCode = generateOtpCode();
        OtpVerification otpVerification;

        if (purpose == OtpPurpose.REGISTER) {
            // Kiểm tra SĐT đã tồn tại chính thức chưa
            if (userRepository.existsByPhoneNumber(phoneNumber)) {
                throw new CustomException(ErrorCode.PHONE_NUMBER_ALREADY_EXISTS, phoneNumber);
            }

            // Tìm bản ghi lưu tạm từ bước /register
            otpVerification = otpVerificationRepository
                    .findTopByPhoneNumberAndPurposeOrderByCreatedAtDesc(phoneNumber, OtpPurpose.REGISTER)
                    .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND,
                            "Chưa nhập thông tin đăng ký. Vui lòng gọi /register trước."));

            if (Boolean.TRUE.equals(otpVerification.getVerified())) {
                throw new CustomException(ErrorCode.USER_EXISTED, "Số điện thoại đã được xác minh");
            }

            // Cập nhật OTP và thời gian hết hạn vào bản ghi tạm
            otpVerification.setOtpCode(otpCode);
            otpVerification.setExpiredAt(LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES));
        } else if (purpose == OtpPurpose.FORGOT_PASSWORD) {
            // Quên mật khẩu: Yêu cầu user phải tồn tại
            if (!userRepository.existsByPhoneNumber(phoneNumber)) {
                throw new CustomException(ErrorCode.USER_NOT_FOUND, phoneNumber);
            }

            // Tạo bản ghi OTP mới cho Forgot Password
            otpVerification = OtpVerification.builder()
                    .phoneNumber(phoneNumber)
                    .otpCode(otpCode)
                    .purpose(OtpPurpose.FORGOT_PASSWORD)
                    .expiredAt(LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES))
                    .verified(false)
                    .build();
        } else {
            throw new CustomException(ErrorCode.TOKEN_INVALID, "Mục đích gửi OTP không hợp lệ");
        }

        otpVerificationRepository.save(otpVerification);

        // TODO: Gửi SMS thật
        log.info("=== DEV MODE: {} OTP for {} is: {} ===", purpose, phoneNumber, otpCode);

        return SendOtpResponse.builder()
                .message("OTP đã gửi thành công")
                .phoneNumber(phoneNumber)
                .otpCode(otpCode)
                .build();
    }

    private String generateOtpCode() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    @Transactional
    public ResetPasswordResponse resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new CustomException(ErrorCode.CONFIRM_PASSWORD_NOT_MATCH);
        }

        OtpVerification otpVerification = otpVerificationRepository
                .findTopByPhoneNumberAndPurposeOrderByCreatedAtDesc(
                        request.getPhoneNumber(),
                        OtpPurpose.FORGOT_PASSWORD)
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND, "OTP not found"));

        if (!Boolean.TRUE.equals(otpVerification.getVerified())) {
            throw new CustomException(ErrorCode.TOKEN_INVALID, "OTP has not been verified yet");
        }

        if (otpVerification.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.TOKEN_EXPIRED);
        }

        if (otpVerification.getResetToken() == null
                || !otpVerification.getResetToken().equals(request.getResetToken())) {
            throw new CustomException(ErrorCode.TOKEN_INVALID, "Invalid reset token");
        }

        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, request.getPhoneNumber()));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Invalidate the reset token after use
        otpVerification.setResetToken(null);
        otpVerificationRepository.save(otpVerification);

        return ResetPasswordResponse.builder()
                .message("Password reset successful")
                .phoneNumber(request.getPhoneNumber())
                .build();
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = Long.valueOf(userIdStr);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, userIdStr));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS, "Incorrect old password");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new CustomException(ErrorCode.CONFIRM_PASSWORD_NOT_MATCH);
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("User ID {} changed their password successfully", userId);
    }

}

package com.example.banckend.notification.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.banckend.auth.entity.User;
import com.example.banckend.auth.repository.UserRepository;
import com.example.banckend.conmon.exception.CustomException;
import com.example.banckend.conmon.exception.ErrorCode;
import com.example.banckend.conmon.response.ApiResponse;
import com.example.banckend.notification.dto.AlertPushDto;
import com.example.banckend.notification.dto.FcmTokenRequest;
import com.example.banckend.notification.service.NotificationPushService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "APIs for push notifications and FCM token management")
public class NotificationController {

    private final UserRepository userRepository;
    private final NotificationPushService notificationPushService;

    // ==================== FCM TOKEN MANAGEMENT ====================

    /**
     * iOS/Android gọi sau khi login thành công hoặc khi FCM token refresh.
     * Lưu FCM token vào DB để server có thể push notification qua Firebase.
     *
     * POST /api/notifications/token
     * Header: Authorization: Bearer <accessToken>
     * Body: { "fcmToken": "..." }
     */
    @PostMapping("/token")
    public ResponseEntity<ApiResponse<Void>> updateFcmToken(@Valid @RequestBody FcmTokenRequest request) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, userId.toString()));

        user.setFcmToken(request.getFcmToken());
        userRepository.save(user);

        log.info("[FCM] Updated FCM token for userId={}", userId);
        return ResponseEntity.ok(ApiResponse.success("FCM token updated successfully", null));
    }

    /**
     * iOS/Android gọi khi user logout.
     * Xóa FCM token để user không nhận push notification nữa.
     *
     * DELETE /api/notifications/token
     * Header: Authorization: Bearer <accessToken>
     */
    @DeleteMapping("/token")
    public ResponseEntity<ApiResponse<Void>> removeFcmToken() {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, userId.toString()));

        user.setFcmToken(null);
        userRepository.save(user);

        log.info("[FCM] Removed FCM token for userId={}", userId);
        return ResponseEntity.ok(ApiResponse.success("FCM token removed successfully", null));
    }

    // ==================== PUSH NOTIFICATION (TEST / INTERNAL) ====================

    /**
     * Endpoint test push notification thủ công.
     *
     * POST /api/notifications/push?userId=1
     * Body: AlertPushDto JSON
     */
    /**
     * Endpoint test: Chỉ cho phép User push thông báo cho chính họ
     * thông qua việc xác thực bằng Access Token.
     */
    @PostMapping("/push")
    public ResponseEntity<ApiResponse<Void>> pushNotification(@RequestBody AlertPushDto alertPushDto) {
        // Lấy userId trực tiếp từ Access Token trong SecurityContext
        Long userId = getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, userId.toString()));

        notificationPushService.pushAlert(user, alertPushDto);
        return ResponseEntity.ok(ApiResponse.success("Notification pushed successfully", null));
    }

    // ==================== HELPER ====================

    private Long getCurrentUserId() {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        return Long.valueOf(userIdStr);
    }
}

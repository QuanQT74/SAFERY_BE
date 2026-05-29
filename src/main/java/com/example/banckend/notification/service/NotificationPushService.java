package com.example.banckend.notification.service;

import java.util.List;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.banckend.auth.entity.FcmToken;
import com.example.banckend.auth.entity.User;
import com.example.banckend.auth.repository.FcmTokenRepository;
import com.example.banckend.notification.dto.AlertPushDto;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPushService {
    private final SimpMessagingTemplate messagingTemplate;
    private final FirebaseMessaging firebaseMessaging;
    private final FcmTokenRepository fcmTokenRepository;

    public void pushAlert(User user, AlertPushDto alertPushDto) {
        // 1. Kiểm tra cấu hình thông báo của user
        if (user.getNotificationSettings() != null
                && Boolean.FALSE.equals(user.getNotificationSettings().getPushEnabled())) {
            log.info("User {} has push notifications disabled. Skipping push.", user.getId());
            return;
        }

        // 2. Gửi thông báo qua WebSocket
        String wsDest = "/topic/device/" + alertPushDto.getDeviceCode() + "/alerts";
        messagingTemplate.convertAndSend(wsDest, alertPushDto);
        log.info("Pushed alert to WebSocket destination {}: {}", wsDest, user.getId());

        // 3. Gửi FCM tới TẤT CẢ các token của user
        List<FcmToken> tokens = fcmTokenRepository.findByUser(user);
        if (tokens.isEmpty()) {
            log.info("[FCM] No tokens found for userId={}", user.getId());
            return;
        }

        boolean isCritical = "CRITICAL".equalsIgnoreCase(alertPushDto.getType());

        for (FcmToken fcmTokenEntity : tokens) {
            String token = fcmTokenEntity.getToken();
            try {
                Message fcmMsg = Message.builder()
                        .setToken(token)
                        .setNotification(Notification.builder()
                                .setTitle(alertPushDto.getTitle())
                                .setBody(alertPushDto.getMessage())
                                .build())
                        .putData("severity", isCritical ? "CRITICAL" : "WARNING")
                        .putData("alertId", alertPushDto.getExtraData() != null
                                && alertPushDto.getExtraData().containsKey("alertId")
                                        ? alertPushDto.getExtraData().get("alertId")
                                        : "")
                        .putAllData(alertPushDto.getExtraData() != null
                                ? alertPushDto.getExtraData()
                                : Map.of())
                        .setApnsConfig(ApnsConfig.builder()
                                .setAps(Aps.builder()
                                        .setSound("default")
                                        .setBadge(1)
                                        .setContentAvailable(true)
                                        .build())
                                .putHeader("apns-priority", "10")
                                .putHeader("apns-push-type", "alert")
                                .build())
                        .setAndroidConfig(AndroidConfig.builder()
                                .setPriority(AndroidConfig.Priority.HIGH)
                                .build())
                        .build();

                firebaseMessaging.send(fcmMsg);
                log.info("[FCM] Sent to userId={} severity={}", user.getId(),
                        isCritical ? "CRITICAL" : "WARNING");

            } catch (FirebaseMessagingException e) {
                log.error("[FCM] Failed for userId={}: {}", user.getId(), e.getMessage());

                // Xóa token không hợp lệ
                if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED
                        || e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT) {
                    fcmTokenRepository.delete(fcmTokenEntity);
                    log.warn("[FCM] Removed invalid token for userId={}", user.getId());
                }
            }
        }
    }

    public void pushSilentStopAlert(User user, String alertId, String deviceCode) {
        List<FcmToken> tokens = fcmTokenRepository.findByUser(user);
        if (tokens.isEmpty()) {
            log.info("[FCM] No tokens found for userId={}", user.getId());
            return;
        }

        for (FcmToken fcmTokenEntity : tokens) {
            try {
                Message silentMessage = Message.builder()
                        .setToken(fcmTokenEntity.getToken())
                        .putData("action", "STOP_ALARM")
                        .putData("deviceCode", deviceCode)
                        .putData("alertId", alertId)
                        .setApnsConfig(ApnsConfig.builder()
                                .setAps(Aps.builder()
                                        .setContentAvailable(true)
                                        .build())
                                .putHeader("apns-priority", "5")
                                .putHeader("apns-push-type", "background")
                                .build())
                        .build();
                firebaseMessaging.send(silentMessage);
                log.info("[FCM] Sent silent stop alert to userId={}", user.getId());
            } catch (FirebaseMessagingException e) {
                log.error("[FCM] Failed to send silent stop alert to userId={}: {}", user.getId(), e.getMessage());
                if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                    fcmTokenRepository.delete(fcmTokenEntity);
                }
            }
        }
    }
}
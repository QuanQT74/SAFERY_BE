package com.example.banckend.notification.service;

import java.util.Map;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.example.banckend.auth.entity.User;
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

    public void pushAlert(User user, AlertPushDto alertPushDto) {
        // 1. Kiểm tra cấu hình thông báo của user (Bám sát entity NotificationSettings)
        if (user.getNotificationSettings() != null
                && Boolean.FALSE.equals(user.getNotificationSettings().getPushEnabled())) {
            log.info("User {} has push notifications disabled. Skipping push.", user.getId());
            return;
        }
        // 2. Gửi thông báo qua WebSocket (nếu user đang online)
        String wsDest = "/topic/device/" + alertPushDto.getDeviceCode() + "/alerts";
        messagingTemplate.convertAndSend(wsDest, alertPushDto);
        log.info("Pushed alert to WebSocket destination {}: {}", wsDest, user.getId());

        // Xác định Priority dựa trên loại Alert (CRITICAL = MAX, WARNING/OFFLINE =
        // HIGH)
        // ── Gửi FCM theo đúng spec iOS ──────────────────────────────
        if (user.getFcmToken() != null && !user.getFcmToken().isEmpty()) {
            try {
                boolean isCritical = "CRITICAL".equalsIgnoreCase(alertPushDto.getType());

                Message fcmMsg = Message.builder()
                        .setToken(user.getFcmToken())

                        // Notification — hiển thị banner
                        .setNotification(Notification.builder()
                                .setTitle(alertPushDto.getTitle())
                                .setBody(alertPushDto.getMessage())
                                .build())

                        // ✅ data payload đúng spec iOS
                        // severity = "CRITICAL" → iOS bật còi lặp liên tục
                        // severity = "WARNING" → iOS chỉ hiện banner
                        .putData("severity", isCritical ? "CRITICAL" : "WARNING")
                        .putData("alertId", alertPushDto.getExtraData() != null
                                && alertPushDto.getExtraData().containsKey("alertId")
                                        ? alertPushDto.getExtraData().get("alertId")
                                        : "")

                        // Thêm toàn bộ extraData nếu có
                        .putAllData(alertPushDto.getExtraData() != null
                                ? alertPushDto.getExtraData()
                                : Map.of())

                        // APNs config cho iOS
                        .setApnsConfig(ApnsConfig.builder()
                                .setAps(Aps.builder()
                                        .setSound("default")
                                        .setBadge(1)
                                        .setContentAvailable(true)
                                        .build())
                                .putHeader("apns-priority", "10")
                                .putHeader("apns-push-type", "alert")
                                .build())

                        // Android config
                        .setAndroidConfig(AndroidConfig.builder()
                                .setPriority(AndroidConfig.Priority.HIGH)
                                .build())

                        .build();

                firebaseMessaging.send(fcmMsg);
                log.info("[FCM] Sent to userId={} severity={}", user.getId(),
                        isCritical ? "CRITICAL" : "WARNING");

            } catch (FirebaseMessagingException e) {
                log.error("[FCM] Failed userId={}: {}", user.getId(), e.getMessage());

                // Xóa token không hợp lệ
                if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                    user.setFcmToken(null);
                    //userRepository.save(user);
                    log.warn("[FCM] Removed invalid token for userId={}", user.getId());
                }
            }
        }
    }
    public void  pushSilentStopAlert(User user , String alartId,String deviceCode){
       if(user.getFcmToken() == null || user.getFcmToken().isEmpty()){
           log.info("User {} has no FCM token. Skipping silent push.", user.getId());
           return;
       }
       try{
            Message sileMessage = Message.builder()
                    .setToken(user.getFcmToken())
                    .putData("action", "STOP_ALERT")
                    .putData("deviceCode", deviceCode)
                    .putData("alertId", alartId)
                    .setApnsConfig(com.google.firebase.messaging.ApnsConfig.builder()
                            .setAps(com.google.firebase.messaging.Aps.builder()
                                    .setContentAvailable(true)
                                    .build())
                            .putHeader("apns-priority", "5") // Silent push
                            .putHeader("apns-push-type", "background")
                            .build())
                    .build();
            firebaseMessaging.send(sileMessage);
            log.info("[FCM] Sent silent stop alert to userId={} for alertId={}", user.getId());
       }catch(Exception e){
              log.error("[FCM] Failed to send silent stop alert to userId={}: {}", user.getId(), e.getMessage());
       }
        
    }
}
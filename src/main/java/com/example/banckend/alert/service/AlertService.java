package com.example.banckend.alert.service;

import com.example.banckend.alert.dto.response.AlertResponse;
import com.example.banckend.alert.entity.Alert;
import com.example.banckend.alert.mapper.AlertMapper;
import com.example.banckend.alert.repository.AlertRepository;
import com.example.banckend.auth.repository.UserRepository;
import com.example.banckend.conmon.enums.AlertSeverity;
import com.example.banckend.conmon.enums.AlertStatus;
import com.example.banckend.conmon.enums.AlertType;
import com.example.banckend.conmon.enums.DeviceStatus;
import com.example.banckend.device.entity.Device;
import com.example.banckend.notification.dto.AlertPushDto;
import com.example.banckend.notification.service.NotificationPushService;
import com.example.banckend.telemetry.dto.request.TelemetryRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final UserRepository userRepository;
    private final AlertRepository alertRepository;
    private final AlertMapper alertMapper;
    private final NotificationPushService notificationPushService;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @Transactional
    public List<AlertResponse> createAlertIfNeeded(Device device, TelemetryRequest request, DeviceStatus status) {
        if (status != DeviceStatus.DANGER || device.getOwnerUserId() == null) {
            return List.of();
        }

        List<Alert> newAlerts = new ArrayList<>();

        if (Boolean.TRUE.equals(request.getFlameDetected())) {
            if (shouldCreateAlert(device.getId(), AlertType.FLAME)) {
                newAlerts.add(createAlertEntity(device, AlertType.FLAME, AlertSeverity.CRITICAL,
                        "Phát hiện lửa", "Phát hiện lửa tại thiết bị " + device.getDeviceName(),
                        null, null));
            }
        }

        if (Boolean.TRUE.equals(request.getAiDetectedFire())) {
            if (shouldCreateAlert(device.getId(), AlertType.AI_FIRE)) {
                newAlerts.add(createAlertEntity(device, AlertType.AI_FIRE, AlertSeverity.CRITICAL,
                        "AI phát hiện cháy", "AI phát hiện cháy tại thiết bị " + device.getDeviceName(),
                        null, null));
            }
        }

        if (request.getGasValue() != null && request.getGasValue() > 1000) {
            if (shouldCreateAlert(device.getId(), AlertType.GAS)) {
                newAlerts.add(createAlertEntity(device, AlertType.GAS, AlertSeverity.CRITICAL,
                        "Nồng độ gas cao", "Nồng độ gas vượt ngưỡng nguy hiểm: " + request.getGasValue() + " ppm",
                        request.getGasValue(), "ppm"));
            }
        }

        if (request.getCoValue() != null && request.getCoValue() > 50) {
            if (shouldCreateAlert(device.getId(), AlertType.CO)) {
                newAlerts.add(createAlertEntity(device, AlertType.CO, AlertSeverity.CRITICAL,
                        "Nồng độ CO cao", "Nồng độ CO vượt ngưỡng nguy hiểm: " + request.getCoValue() + " ppm",
                        request.getCoValue(), "ppm"));
            }
        }

        if (request.getTemperature() != null && request.getTemperature() > 50) {
            if (shouldCreateAlert(device.getId(), AlertType.TEMPERATURE)) {
                newAlerts.add(createAlertEntity(device, AlertType.TEMPERATURE, AlertSeverity.CRITICAL,
                        "Nhiệt độ cao", "Nhiệt độ vượt ngưỡng nguy hiểm: " + request.getTemperature() + "°C",
                        request.getTemperature(), "°C"));
            }
        }

        if (request.getHumidity() != null && request.getHumidity() > 80) {
            if (shouldCreateAlert(device.getId(), AlertType.HUMIDITY)) {
                newAlerts.add(createAlertEntity(device, AlertType.HUMIDITY, AlertSeverity.WARNING,
                        "Độ ẩm cao", "Độ ẩm vượt ngưỡng: " + request.getHumidity() + "%",
                        request.getHumidity(), "%"));
            }
        }

        if (!newAlerts.isEmpty()) {
            alertRepository.saveAll(newAlerts);
            List<AlertResponse> responses = newAlerts.stream().map(alertMapper::toResponse).toList();

            userRepository.findById(device.getOwnerUserId())
                    .ifPresent(user -> {
                        for (AlertResponse ar : responses) {
                            AlertPushDto alertPushDto = AlertPushDto.builder()
                                    .deviceCode(device.getDeviceCode())
                                    .title(ar.getTitle())
                                    .message(ar.getMessage())
                                    .type(ar.getSeverity().name())
                                    .deviceCode(device.getDeviceCode())
                                    .extraData(Map.of("alertId", ar.getId().toString()))
                                    .build();
                            notificationPushService.pushAlert(user, alertPushDto);
                        }

                    });
            return responses;
        }

        return List.of();
    }

    @Transactional
    public AlertResponse createOfflineAlert(Device device) {
        if (shouldCreateAlert(device.getId(), AlertType.OFFLINE)) {
            return null;
        }
        Alert alert = createAlertEntity(device, AlertType.OFFLINE, AlertSeverity.WARNING,
                "Thiết bị mất kết nối", "Thiết bị " + device.getDeviceName() + " đã offline.", null, null);
        alertRepository.save(alert);
        AlertResponse response = alertMapper.toResponse(alert);

        // Push thông báo OFFLINE cho user (nếu có owner)
        if (device.getOwnerUserId() != null) {
            userRepository.findById(device.getOwnerUserId()).ifPresent(user -> {
                AlertPushDto alertPushDto = AlertPushDto.builder()
                        .deviceCode(device.getDeviceCode())
                        .title(response.getTitle())
                        .message(response.getMessage())
                        .type(response.getSeverity().name())
                        .extraData(Map.of("alertId", response.getId().toString()))
                        .build();
                notificationPushService.pushAlert(user, alertPushDto);
            });
        }

        return response;
    }

    private boolean shouldCreateAlert(Long deviceId, AlertType type) {
        return alertRepository.findFirstByDeviceIdAndTypeAndAcknowledgedFalseOrderByCreatedAtEventDesc(deviceId, type)
                .map(alert -> alert.getCreatedAtEvent().isBefore(LocalDateTime.now().minusMinutes(2)))
                .orElse(true);
    }

    private Alert createAlertEntity(Device device, AlertType type, AlertSeverity severity,
            String title, String message, Double sensorValue, String unit) {
        Alert alert = new Alert();
        alert.setDevice(device);
        alert.setType(type);
        alert.setSeverity(severity);
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setSensorValue(sensorValue);
        alert.setUnit(unit);
        alert.setAcknowledged(false);
        alert.setCreatedAtEvent(LocalDateTime.now());
        log.info("[ALERT] Preparing alert for device {}: {} - {}", device.getDeviceCode(), type, title);
        return alert;
    }

    @Transactional(readOnly = true)
    public Page<AlertResponse> getFilteredAlerts(Long userId, String severityStr, String statusStr, Pageable pageable) {
        AlertSeverity severity = null;
        if (severityStr != null && !severityStr.equalsIgnoreCase("ALL")) {
            try {
                severity = AlertSeverity.valueOf(severityStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                severity = null;
            }
        }

        Boolean acknowledged = null;
        if (statusStr != null && !statusStr.equalsIgnoreCase("ALL")) {
            try {
                AlertStatus status = AlertStatus.valueOf(statusStr.toUpperCase());
                acknowledged = (status == AlertStatus.RESOLVED) ? true : false;
            } catch (IllegalArgumentException e) {
                acknowledged = null;
            }
        }

        log.info("[ALERT_FILTER] userId={}, severity={}, acknowledged={}", userId, severity, acknowledged);

        Page<Alert> alerts = alertRepository.findFilteredAlerts(userId, severity, acknowledged, pageable);

        log.info("[ALERT_FILTER] Found {} alerts.", alerts.getTotalElements());

        return alerts.map(alertMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public AlertResponse getLatestAlertByUserId(Long userId) {
        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAtEvent"));
        Page<Alert> alerts = alertRepository.findByDeviceOwnerUserIdOrderByCreatedAtEventDesc(userId, pageable);
        if (alerts.isEmpty()) {
            return null;
        }
        return alertMapper.toResponse(alerts.getContent().get(0));
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> getActiveAlertsByUserId(Long userId) {
        List<Alert> alerts = alertRepository
                .findByDeviceOwnerUserIdAndAcknowledgedFalseOrderByCreatedAtEventDesc(userId);
        return alertMapper.toResponseList(alerts);
    }

    @Transactional
    public AlertResponse acknowledgeAlert(Long alertId, Long userId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        // Validate: alert thuộc về device của user
        Device device = alert.getDevice();
        if (device.getOwnerUserId() == null || !device.getOwnerUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        alert.setAcknowledged(true);
        alert.setResolvedAt(LocalDateTime.now());
        alertRepository.save(alert);

        // -------------------------------------------------------------
        // TÍCH HỢP TẮT CÒI ĐỒNG BỘ
        // -------------------------------------------------------------

        // 1. Gửi WebSocket để tắt còi các máy đang mở App (Foreground)
        Map<String, Object> syncPayload = Map.of(
                "action", "STOP_ALARM",
                "alertId", alertId.toString(),
                "deviceCode", device.getDeviceCode());
        messagingTemplate.convertAndSend("/topic/device/" + device.getDeviceCode() + "/alerts", (Object) syncPayload);

        // 2. Gửi Silent Push để tắt còi các máy đang khóa màn hình/background
        // Tìm TẤT CẢ các user được quyền nhận thông báo của thiết bị này.
        // Hiện tại thiết bị chỉ có 1 Owner, nên lấy chính Owner đó gửi Push tắt.
        userRepository.findById(device.getOwnerUserId()).ifPresent(owner -> {

            notificationPushService.pushSilentStopAlert(owner, alertId.toString(), device.getDeviceCode());
        });

        // ------------------------------------------------------------
        return alertMapper.toResponse(alert);
    }

    @Transactional(readOnly = true)
    public long countUnreadByUserId(Long userId) {
        // Chỉ đếm trong 20 alert mới nhất (không đếm toàn bộ database)
        // Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC,
        // "createdAtEvent"));
        // Page<Alert> alerts =
        // alertRepository.findByDeviceOwnerUserIdOrderByCreatedAtEventDesc(userId,
        // pageable);
        // return alerts.getContent().stream()
        // .filter(a -> !Boolean.TRUE.equals(a.getAcknowledged()))
        // .count();
        return alertRepository.countByDeviceOwnerUserId(userId);
    }

    @Transactional
    public void deleteAlert(Long alertId, Long userId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        Device device = alert.getDevice();
        if (device.getOwnerUserId() == null || !device.getOwnerUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        alertRepository.delete(alert);
    }
}
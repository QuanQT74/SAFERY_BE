package com.example.banckend.device.dto.response;

import com.example.banckend.conmon.enums.DeviceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceTelemetryResponse {
    private String deviceCode;
    private Double gasValue;
    private Double coValue;
    private Double temperature;
    private Double humidity;
    private Boolean flameDetected;
    private Boolean fanStatus;
    private Boolean alarmArmed;
    private Boolean aiDetectedFire;
    private DeviceStatus overallStatus;
    private Boolean online;
    private LocalDateTime lastSeenAt;
    private LocalDateTime stateUpdatedAt;

    // Trạng thái đang chờ xử lý (sẽ được sync khi ESP32 gửi telemetry)
    private Boolean pendingFanStatus;
    private Boolean pendingAlarmStatus;

    // Trạng thái command: PENDING, SENT, ACKNOWLEDGED, FAILED
    private String commandStatus;
}
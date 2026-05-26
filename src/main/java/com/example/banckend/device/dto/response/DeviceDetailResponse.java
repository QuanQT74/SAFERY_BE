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
public class DeviceDetailResponse {

    private Long id;
    private String deviceCode;
    private String deviceName;
    private String nickname;
    private String esp32Serial;
    private String firmwareVersion;
    private DeviceStatus status;
    private Boolean online;
    private LocalDateTime lastSeenAt;
    private CameraInfoResponse cameraInfo;
    private SensorThresholdResponse sensorThreshold;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CameraInfoResponse {
        private Long id;
        private String streamUrl;
        private String snapshotUrl;
        private Boolean online;
        private LocalDateTime lastUpdatedAt;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SensorThresholdResponse {
        private Long id;
        private Double gasWarningThreshold;
        private Double gasDangerThreshold;
        private Double coWarningThreshold;
        private Double coDangerThreshold;
        private Double temperatureWarningThreshold;
        private Double temperatureDangerThreshold;
    }
}

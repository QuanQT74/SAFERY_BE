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
public class LatestReadingResponse {

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
}

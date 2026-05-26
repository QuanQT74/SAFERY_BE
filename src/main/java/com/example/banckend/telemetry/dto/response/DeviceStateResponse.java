package com.example.banckend.telemetry.dto.response;

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
public class DeviceStateResponse {

    private Long id;
    private Long deviceId;
    private Double gasValue;
    private Double coValue;
    private Double temperature;
    private Double humidity;
    private Boolean flameDetected;
    private Boolean fanStatus;
    private Boolean autoMode;
    private Boolean alarmArmed;
    private DeviceStatus overallStatus;
    private LocalDateTime stateUpdatedAt;
    private Boolean aiDetectedFire;
}

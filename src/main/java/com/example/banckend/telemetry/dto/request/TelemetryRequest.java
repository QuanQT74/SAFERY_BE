package com.example.banckend.telemetry.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryRequest {

    @NotBlank(message = "Device code is required")
    private String deviceCode;

    @NotBlank(message = "Device secret is required")
    private String deviceSecret;

    @NotNull(message = "Gas value is required")
    private Double gasValue;

    @NotNull(message = "CO value is required")
    private Double coValue;

    @NotNull(message = "Temperature is required")
    private Double temperature;

    @NotNull(message = "Humidity is required")
    private Double humidity;

    @NotNull(message = "Flame detected is required")
    private Boolean flameDetected;

    @NotNull(message = "Fan status is required")
    private Boolean fanStatus;
    
    @NotNull(message = "Alarm armed is required")
    private Boolean alarmArmed;

    @NotNull(message = "AI detected fire is required")
    private Boolean aiDetectedFire;
}

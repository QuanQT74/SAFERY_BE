package com.example.banckend.telemetry.dto.response;

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
public class TelemetryHistoryResponse {
    private Double gasValue;
    private Double coValue;
    private Double temperature;
    private Double humidity;
    private Boolean flameDetected;
    private Boolean aiDetectedFire;
    private LocalDateTime recordedAt;
}

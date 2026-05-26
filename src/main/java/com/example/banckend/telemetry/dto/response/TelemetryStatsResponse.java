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
public class TelemetryStatsResponse {
    // Gas
    private Double currentGas;
    private Double maxGas;
    private Double minGas;
    private Double avgGas;
    private Boolean gasWarning;
    // CO
    private Double currentCo;
    private Double maxCo;
    private Double minCo;
    private Double avgCo;
    private Boolean coWarning;
    // Temperature
    private Double currentTemp;
    private Double maxTemp;
    private Double minTemp;
    private Double avgTemp;
    private Boolean tempWarning;
    // Humidity
    private Double currentHumidity;
    private Double maxHumidity;
    private Double minHumidity;
    private Double avgHumidity;
    // Flame detection (boolean, count of true)
    private Boolean flameDetected;
    // AI fire detection (boolean, count of true)
    private Boolean aiDetectedFire;
    // Period range
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
}
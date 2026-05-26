package com.example.banckend.telemetry.dto.response;

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
public class SensorStatsResponse {
    private String sensor;
    private Double currentValue;
    private Double maxValue;
    private Double minValue;
    private Double avgValue;
    private String period;
}
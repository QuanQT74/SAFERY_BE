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
public class AggregatedDataResponse {
    private LocalDateTime timestamp;

    // Gas
    private Double maxGas;
    private Double minGas;
    private Double avgGas;

    // CO
    private Double maxCo;
    private Double minCo;
    private Double avgCo;

    // Temperature
    private Double maxTemp;
    private Double minTemp;
    private Double avgTemp;

    // Humidity
    private Double maxHumidity;
    private Double minHumidity;
    private Double avgHumidity;

    public static AggregatedDataResponse fromObjectArray(Object[] row) {
        return AggregatedDataResponse.builder()
                .timestamp(parseTimestamp(row[0]))
                .maxGas(parseDouble(row[1]))
                .minGas(parseDouble(row[2]))
                .avgGas(parseDouble(row[3]))
                .maxCo(parseDouble(row[4]))
                .minCo(parseDouble(row[5]))
                .avgCo(parseDouble(row[6]))
                .maxTemp(parseDouble(row[7]))
                .minTemp(parseDouble(row[8]))
                .avgTemp(parseDouble(row[9]))
                .maxHumidity(parseDouble(row[10]))
                .minHumidity(parseDouble(row[11]))
                .avgHumidity(parseDouble(row[12]))
                .build();
    }

    private static LocalDateTime parseTimestamp(Object obj) {
        if (obj == null) return null;
        return LocalDateTime.parse(obj.toString());
    }

    private static Double parseDouble(Object obj) {
        if (obj == null) return 0.0;
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        try {
            return Double.parseDouble(obj.toString());
        } catch (Exception e) {
            return 0.0;
        }
    }
}
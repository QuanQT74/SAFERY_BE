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
public class SensorAggregatedResponse {
    private LocalDateTime timestamp;
    private Double max;
    private Double min;
    private Double avg;
    private Double avgValue; // Giá trị trung bình để vẽ biểu đồ (lấy từ avg)

    public static SensorAggregatedResponse fromObjectArray(Object[] row) {
        Double avgVal = parseDouble(row[3]);
        return SensorAggregatedResponse.builder()
                .timestamp(parseTimestamp(row[0]))
                .max(parseDouble(row[1]))
                .min(parseDouble(row[2]))
                .avg(avgVal)
                .avgValue(avgVal) // Dùng avg làm giá trị chính để vẽ biểu đồ
                .build();
    }

    private static LocalDateTime parseTimestamp(Object obj) {
        if (obj == null) return null;
        try {
            return LocalDateTime.parse(obj.toString().replace(" ", "T"));
        } catch (Exception e) {
            return null;
        }
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

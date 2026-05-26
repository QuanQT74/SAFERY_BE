package com.example.banckend.alert.dto.response;

import com.example.banckend.conmon.enums.AlertSeverity;
import com.example.banckend.conmon.enums.AlertType;
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
public class AlertResponse {

    private Long id;
    private Long deviceId;
    private String deviceName;
    private AlertType type;
    private AlertSeverity severity;
    private String title;
    private String message;
    private Double sensorValue;
    private String unit;
    private Boolean acknowledged;
    private LocalDateTime createdAtEvent;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
}

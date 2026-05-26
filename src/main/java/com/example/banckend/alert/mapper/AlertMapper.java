package com.example.banckend.alert.mapper;

import com.example.banckend.alert.dto.response.AlertResponse;
import com.example.banckend.alert.entity.Alert;
import com.example.banckend.device.entity.Device;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AlertMapper {

    public AlertResponse toResponse(Alert alert) {
        return AlertResponse.builder()
                .id(alert.getId())
                .deviceId(alert.getDevice().getId())
                .deviceName(alert.getDevice().getDeviceName())
                .type(alert.getType())
                .severity(alert.getSeverity())
                .title(alert.getTitle())
                .message(alert.getMessage())
                .sensorValue(alert.getSensorValue())
                .unit(alert.getUnit())
                .acknowledged(alert.getAcknowledged())
                .createdAtEvent(alert.getCreatedAtEvent())
                .resolvedAt(alert.getResolvedAt())
                .createdAt(alert.getCreatedAt())
                .build();
    }

    public Alert toEntity(Device device, AlertResponse response) {
        Alert alert = new Alert();
        alert.setDevice(device);
        alert.setType(response.getType());
        alert.setSeverity(response.getSeverity());
        alert.setTitle(response.getTitle());
        alert.setMessage(response.getMessage());
        alert.setSensorValue(response.getSensorValue());
        alert.setUnit(response.getUnit());
        alert.setAcknowledged(response.getAcknowledged());
        alert.setCreatedAtEvent(response.getCreatedAtEvent());
        return alert;
    }

    public List<AlertResponse> toResponseList(List<Alert> alerts) {
        return alerts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}

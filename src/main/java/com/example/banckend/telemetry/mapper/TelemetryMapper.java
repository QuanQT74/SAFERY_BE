package com.example.banckend.telemetry.mapper;

import com.example.banckend.conmon.enums.DeviceStatus;
import com.example.banckend.device.entity.Device;
import com.example.banckend.telemetry.dto.request.TelemetryRequest;
import com.example.banckend.telemetry.dto.response.DeviceStateResponse;
import com.example.banckend.telemetry.dto.response.SensorReadingResponse;
import com.example.banckend.telemetry.dto.response.TelemetryHistoryResponse;
import com.example.banckend.telemetry.entity.DeviceLatestState;
import com.example.banckend.telemetry.entity.SensorReading;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TelemetryMapper {

    public SensorReading toEntity(Device device, TelemetryRequest request) {
        SensorReading reading = new SensorReading();
        reading.setDevice(device);
        reading.setGasValue(request.getGasValue());
        reading.setCoValue(request.getCoValue());
        reading.setTemperature(request.getTemperature());
        reading.setHumidity(request.getHumidity());
        reading.setFlameDetected(request.getFlameDetected());
        reading.setFanStatus(request.getFanStatus());
        reading.setAlarmArmed(request.getAlarmArmed());
        reading.setAiDetectedFire(request.getAiDetectedFire());
        return reading;
    }

    public void updateLatestState(DeviceLatestState state, Device device,
            TelemetryRequest request,
            DeviceStatus overallStatus) {
        state.setDevice(device);
        state.setGasValue(request.getGasValue());
        state.setCoValue(request.getCoValue());
        state.setTemperature(request.getTemperature());
        state.setHumidity(request.getHumidity());
        state.setFlameDetected(request.getFlameDetected());
        state.setFanStatus(request.getFanStatus());
        state.setAlarmArmed(request.getAlarmArmed());
        state.setAiDetectedFire(request.getAiDetectedFire());
        state.setOverallStatus(overallStatus);
        state.setStateUpdatedAt(LocalDateTime.now());
    }

    public SensorReadingResponse toResponse(SensorReading reading) {
        return SensorReadingResponse.builder()
                .id(reading.getId())
                .deviceId(reading.getDevice().getId())
                .gasValue(reading.getGasValue())
                .coValue(reading.getCoValue())
                .temperature(reading.getTemperature())
                .humidity(reading.getHumidity())
                .flameDetected(reading.getFlameDetected())
                .fanStatus(reading.getFanStatus())
                .alarmArmed(reading.getAlarmArmed())
                .aiDetectedFire(reading.getAiDetectedFire())
                .recordedAt(reading.getRecordedAt())
                .build();
    }

    public TelemetryHistoryResponse toHistoryResponse(SensorReading reading) {
        return TelemetryHistoryResponse.builder()
                .gasValue(reading.getGasValue())
                .coValue(reading.getCoValue())
                .temperature(reading.getTemperature())
                .humidity(reading.getHumidity())
                .flameDetected(reading.getFlameDetected())
                .aiDetectedFire(reading.getAiDetectedFire())
                .recordedAt(reading.getRecordedAt())
                .build();
    }

    public DeviceStateResponse toStateResponse(DeviceLatestState state) {
        return DeviceStateResponse.builder()
                .id(state.getId())
                .deviceId(state.getDevice().getId())
                .gasValue(state.getGasValue())
                .coValue(state.getCoValue())
                .temperature(state.getTemperature())
                .humidity(state.getHumidity())
                .flameDetected(state.getFlameDetected())
                .fanStatus(state.getFanStatus())
                .alarmArmed(state.getAlarmArmed())
                .aiDetectedFire(state.getAiDetectedFire())
                .overallStatus(state.getOverallStatus())
                .stateUpdatedAt(state.getStateUpdatedAt())
                .build();
    }
}
package com.example.banckend.device.mapper;


import com.example.banckend.device.dto.response.DeviceResponse;
import com.example.banckend.device.dto.response.DeviceTelemetryResponse;
import com.example.banckend.device.dto.response.LatestReadingResponse;
import com.example.banckend.device.entity.Device;
import com.example.banckend.telemetry.entity.DeviceLatestState;
import org.springframework.stereotype.Component;

@Component
public class DeviceMapper {

    public LatestReadingResponse toLatestReadingResponse(Device device, DeviceLatestState state) {
        return LatestReadingResponse.builder()
                .deviceCode(device.getDeviceCode())
                .gasValue(state.getGasValue())
                .coValue(state.getCoValue())
                .temperature(state.getTemperature())
                .humidity(state.getHumidity())
                .flameDetected(state.getFlameDetected())
                .fanStatus(state.getFanStatus())
                .alarmArmed(state.getAlarmArmed())
                .aiDetectedFire(state.getAiDetectedFire())
                .overallStatus(state.getOverallStatus())
                .online(device.getOnline())
                .lastSeenAt(device.getLastSeenAt())
                .stateUpdatedAt(state.getStateUpdatedAt())
                .build();
    }

    public DeviceTelemetryResponse toTelemetryResponse(Device device, DeviceLatestState state) {
        return DeviceTelemetryResponse.builder()
                .deviceCode(device.getDeviceCode())
                .gasValue(state.getGasValue())
                .coValue(state.getCoValue())
                .temperature(state.getTemperature())
                .humidity(state.getHumidity())
                .flameDetected(state.getFlameDetected())
                .fanStatus(state.getFanStatus())
                .alarmArmed(state.getAlarmArmed())
                .aiDetectedFire(state.getAiDetectedFire())
                .overallStatus(state.getOverallStatus())
                .online(device.getOnline())
                .lastSeenAt(device.getLastSeenAt())
                .stateUpdatedAt(state.getStateUpdatedAt())
                .pendingFanStatus(state.getPendingFanStatus())
                .pendingAlarmStatus(state.getPendingAlarmStatus())
                .build();
    }

    public DeviceResponse toResponse(Device device) {
        return DeviceResponse.builder()
                .id(device.getId())
                .deviceCode(device.getDeviceCode())
                .deviceName(device.getDeviceName())
                .nickname(device.getNickname())
                .status(device.getStatus())
                .online(device.getOnline())
                .lastSeenAt(device.getLastSeenAt())
                .build();
    }
}

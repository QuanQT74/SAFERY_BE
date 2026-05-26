package com.example.banckend.telemetry.service;

import com.example.banckend.alert.dto.response.AlertResponse;
import com.example.banckend.alert.service.AlertService;
import com.example.banckend.websocket.DeviceWebSocketService;
import com.example.banckend.command.dto.response.DeviceCommandResponse;
import com.example.banckend.command.mapper.DeviceCommandMapper;
import com.example.banckend.command.repository.DeviceCommandRepository;
import com.example.banckend.conmon.enums.CommandStatus;
import com.example.banckend.conmon.enums.DeviceErrorCode;
import com.example.banckend.conmon.enums.DeviceStatus;
import com.example.banckend.conmon.exception.BadRequestException;
import com.example.banckend.device.entity.Device;
import com.example.banckend.device.repository.DeviceRepository;
import com.example.banckend.telemetry.dto.request.TelemetryRequest;
import com.example.banckend.telemetry.dto.response.TelemetryResponse;
import com.example.banckend.telemetry.entity.DeviceLatestState;
import com.example.banckend.telemetry.entity.SensorReading;
import com.example.banckend.telemetry.repository.DeviceLatestStateRepository;
import com.example.banckend.telemetry.repository.SensorReadingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TelemetryService {

    private final SensorReadingRepository sensorReadingRepository;
    private final DeviceLatestStateRepository deviceLatestStateRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceCommandRepository commandRepository;
    private final DeviceCommandMapper commandMapper;
    private final AlertService alertService;
    private final DeviceWebSocketService deviceWebSocketService;

    @Transactional
    public TelemetryResponse processTelemetry(TelemetryRequest request) {
        System.out.println("=== TELEMETRY START ===");
        System.out.println("deviceCode = " + request.getDeviceCode());
        System.out.println("deviceSecret = " + request.getDeviceSecret());
        System.out.println("gasValue = " + request.getGasValue());
        System.out.println("coValue = " + request.getCoValue());
        System.out.println("temperature = " + request.getTemperature());
        System.out.println("humidity = " + request.getHumidity());
        System.out.println("flameDetected = " + request.getFlameDetected());
        System.out.println("fanStatus = " + request.getFanStatus());
        System.out.println("alarmArmed = " + request.getAlarmArmed());
        System.out.println("aiDetectedFire = " + request.getAiDetectedFire());

        Device device = deviceRepository.findByDeviceCode(request.getDeviceCode())
                .orElseThrow(() -> new BadRequestException(DeviceErrorCode.DEVICE_NOT_FOUND));

        if (!device.getDeviceSecret().equals(request.getDeviceSecret())) {
            System.err.println("[AUTH] Wrong Secret for device " + request.getDeviceCode() + "! Expected: " + device.getDeviceSecret() + ", but got: " + request.getDeviceSecret());
            throw new BadRequestException(DeviceErrorCode.INVALID_DEVICE_SECRET);
        }

        LocalDateTime now = LocalDateTime.now();
        DeviceStatus overallStatus = calculateOverallStatus(request);

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
        reading.setRecordedAt(now);

        sensorReadingRepository.save(reading);
        System.out.println("Saved SensorReading id = " + reading.getId());

        DeviceLatestState latestState = deviceLatestStateRepository.findByDeviceId(device.getId())
                .orElseGet(() -> {
                    DeviceLatestState newState = new DeviceLatestState();
                    newState.setDevice(device);
                    return newState;
                });

        latestState.setGasValue(request.getGasValue());
        latestState.setCoValue(request.getCoValue());
        latestState.setTemperature(request.getTemperature());
        latestState.setHumidity(request.getHumidity());
        latestState.setFlameDetected(request.getFlameDetected());
        latestState.setFanStatus(request.getFanStatus());
        latestState.setAlarmArmed(request.getAlarmArmed());
        latestState.setAiDetectedFire(request.getAiDetectedFire());
        latestState.setOverallStatus(overallStatus);
        latestState.setStateUpdatedAt(now);

        // Clear pending status nếu ESP32 đã thực hiện xong (giá trị thực tế = pending)
        if (latestState.getPendingFanStatus() != null &&
            latestState.getPendingFanStatus().equals(request.getFanStatus())) {
            latestState.setPendingFanStatus(null);
        }
        if (latestState.getPendingAlarmStatus() != null &&
            latestState.getPendingAlarmStatus().equals(request.getAlarmArmed())) {
            latestState.setPendingAlarmStatus(null);
        }

        deviceLatestStateRepository.save(latestState);
        System.out.println("Saved DeviceLatestState id = " + latestState.getId());

        device.setOnline(true);
        device.setLastSeenAt(now);
        device.setStatus(overallStatus);
        deviceRepository.save(device);

        // Tạo alert nếu phát hiện DANGER và push lên WebSocket
        List<AlertResponse> newAlerts = alertService.createAlertIfNeeded(device, request, overallStatus);
        for (AlertResponse alert : newAlerts) {
            deviceWebSocketService.pushAlertToApp(device.getDeviceCode(), alert);
        }

        System.out.println("Updated Device id = " + device.getId());

        System.out.println("[TELEMETRY] deviceId=" + device.getId() + ", ownerUserId=" + device.getOwnerUserId());
        System.out.println("[TELEMETRY] Calling getAndMarkPendingCommands...");
        List<DeviceCommandResponse> pendingCommands = getAndMarkPendingCommands(device.getId());
        System.out.println("[TELEMETRY] Pending commands: " + pendingCommands.size());

        System.out.println("=== TELEMETRY END ===");
        return TelemetryResponse.builder()
                .success(true)
                .pendingCommands(pendingCommands)
                .nickname(device.getNickname())
                .build();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<DeviceCommandResponse> getAndMarkPendingCommands(Long deviceId) {
        System.out.println("[COMMAND] Checking pending commands for deviceId: " + deviceId);
        var pendingCommands = commandRepository.findByDeviceIdAndStatus(deviceId, CommandStatus.PENDING);
        System.out.println("[COMMAND] Found " + pendingCommands.size() + " pending commands (status=PENDING)");

        // Đánh dấu SENT ngay sau khi lấy ra → tránh gửi lại lần telemetry tiếp theo
        if (!pendingCommands.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            pendingCommands.forEach(cmd -> {
                cmd.setStatus(CommandStatus.SENT);
                cmd.setExecutedAt(now);
            });
            commandRepository.saveAll(pendingCommands);
            System.out.println("[COMMAND] Marked " + pendingCommands.size() + " commands as SENT");
        }

        return commandMapper.toResponseList(pendingCommands);
    }

    @Transactional
    public void acknowledgeCommand(String deviceCode, String deviceSecret, Long commandId) {
        Device device = deviceRepository.findByDeviceCode(deviceCode)
                .orElseThrow(() -> new BadRequestException(DeviceErrorCode.DEVICE_NOT_FOUND));

        if (!device.getDeviceSecret().equals(deviceSecret)) {
            throw new BadRequestException(DeviceErrorCode.INVALID_DEVICE_SECRET);
        }

        var command = commandRepository.findById(commandId)
                .orElseThrow(() -> new BadRequestException(DeviceErrorCode.RESOURCE_NOT_FOUND));

        if (!command.getDevice().getId().equals(device.getId())) {
            throw new BadRequestException(DeviceErrorCode.ACCESS_DENIED);
        }

        command.setStatus(CommandStatus.ACKNOWLEDGED);
        command.setExecutedAt(LocalDateTime.now());
        commandRepository.save(command);
    }

    private DeviceStatus calculateOverallStatus(TelemetryRequest request) {
        // OSHA standard thresholds
        // DANGER: gas>1000 OR co>50 OR temperature>50 OR flame OR aiDetectedFire
        boolean isDanger = Boolean.TRUE.equals(request.getFlameDetected())
                || Boolean.TRUE.equals(request.getAiDetectedFire())
                || request.getGasValue() > 1000
                || request.getCoValue() > 50
                || request.getTemperature() > 50;

        if (isDanger) {
            return DeviceStatus.DANGER;
        }

        // WARNING: gas>500 OR co>20 OR temperature>40
        boolean isWarning = request.getGasValue() > 500
                || request.getCoValue() > 20
                || request.getTemperature() > 40;

        if (isWarning) {
            return DeviceStatus.WARNING;
        }

        return DeviceStatus.SAFE;
    }
}
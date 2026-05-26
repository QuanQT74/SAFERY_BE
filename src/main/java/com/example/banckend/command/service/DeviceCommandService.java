package com.example.banckend.command.service;

import com.example.banckend.command.dto.response.DeviceCommandResponse;
import com.example.banckend.command.entity.DeviceCommand;
import com.example.banckend.command.mapper.DeviceCommandMapper;
import com.example.banckend.command.repository.DeviceCommandRepository;
import com.example.banckend.conmon.enums.CommandStatus;
import com.example.banckend.conmon.enums.CommandType;
import com.example.banckend.conmon.enums.DeviceErrorCode;
import com.example.banckend.conmon.exception.BadRequestException;
import com.example.banckend.conmon.exception.ResourceNotFoundException;
import com.example.banckend.device.entity.Device;
import com.example.banckend.device.repository.DeviceRepository;
import com.example.banckend.notification.dto.AlertPushDto;
import com.example.banckend.telemetry.entity.DeviceLatestState;
import com.example.banckend.telemetry.repository.DeviceLatestStateRepository;
import com.example.banckend.websocket.DeviceWebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.example.banckend.device.mapper.DeviceMapper;
import com.example.banckend.device.dto.response.DeviceTelemetryResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceCommandService {

    private final DeviceCommandRepository commandRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceLatestStateRepository deviceLatestStateRepository;
    private final DeviceCommandMapper commandMapper;
    private final DeviceMapper deviceMapper;
    @Lazy
    private final DeviceWebSocketService webSocketService;

    @Transactional
    public DeviceCommandResponse createFanCommand(String deviceCode, Boolean fanStatus, Long userId) {
        Device device = deviceRepository.findByDeviceCode(deviceCode)
                .orElseThrow(() -> new ResourceNotFoundException(DeviceErrorCode.DEVICE_NOT_FOUND));

        log.info("[COMMAND] deviceCode={}, userId={}, fanStatus={}", deviceCode, userId, fanStatus);

        if (device.getOwnerUserId() == null || !device.getOwnerUserId().equals(userId)) {
            throw new BadRequestException(DeviceErrorCode.ACCESS_DENIED);
        }

        // Cập nhật trạng thái chờ trong DeviceLatestState
        DeviceLatestState latestState = deviceLatestStateRepository.findByDeviceId(device.getId())
                .orElseThrow(() -> new ResourceNotFoundException(DeviceErrorCode.RESOURCE_NOT_FOUND));

        latestState.setPendingFanStatus(Boolean.TRUE.equals(fanStatus));
        deviceLatestStateRepository.save(latestState);

        // Tạo command
        CommandType commandType = Boolean.TRUE.equals(fanStatus) ? CommandType.FAN_ON : CommandType.FAN_OFF;

        DeviceCommand command = new DeviceCommand();
        command.setDevice(device);
        command.setCommandType(commandType);
        command.setCommandValue(fanStatus ? "ON" : "OFF");
        command.setStatus(CommandStatus.PENDING);

        commandRepository.save(command);

        // Chuẩn bị các dữ liệu cần push
        DeviceCommandResponse response = commandMapper.toResponse(command);
        DeviceTelemetryResponse latestStateResponse = deviceMapper.toTelemetryResponse(device, latestState);
        

        // Đăng ký push sau khi transaction commit xong
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                log.info("[WS] Pushing FAN command and Latest State to device {} after commit", deviceCode);
                // 1. Gửi lệnh cho ESP32
                webSocketService.pushCommandToDevice(deviceCode, response);
                // 2. Gửi trạng thái mới nhất cho App (Để App hiện trạng thái PENDING ngay, không bị đá nút)
                webSocketService.pushLatestStateToApp(deviceCode, latestStateResponse);
                AlertPushDto alertPushDto = AlertPushDto.builder()
                        .title("Fan Command Issued")
                        .message("A command to turn " + (fanStatus ? "ON" : "OFF") + " the fan has been issued.")
                        .extraData(Map.of("commandId", response.getId().toString(), "status","PENDING"))
                        .build();
                // Push alert to app
                webSocketService.pushAlertToApp(deviceCode, alertPushDto);
            }
        });

        return response;
    }

    @Transactional
    public DeviceCommandResponse createAlarmCommand(String deviceCode, Boolean alarmArmed, Long userId) {
        Device device = deviceRepository.findByDeviceCode(deviceCode)
                .orElseThrow(() -> new ResourceNotFoundException(DeviceErrorCode.DEVICE_NOT_FOUND));

        if (device.getOwnerUserId() == null || !device.getOwnerUserId().equals(userId)) {
            throw new BadRequestException(DeviceErrorCode.ACCESS_DENIED);
        }

        // Cập nhật trạng thái chờ trong DeviceLatestState
        DeviceLatestState latestState = deviceLatestStateRepository.findByDeviceId(device.getId())
                .orElseThrow(() -> new ResourceNotFoundException(DeviceErrorCode.RESOURCE_NOT_FOUND));
        latestState.setPendingAlarmStatus(Boolean.TRUE.equals(alarmArmed));
        deviceLatestStateRepository.save(latestState);

        // Tạo command
        CommandType commandType = Boolean.TRUE.equals(alarmArmed) ? CommandType.ALARM_ON : CommandType.ALARM_OFF;

        DeviceCommand command = new DeviceCommand();
        command.setDevice(device);
        command.setCommandType(commandType);
        command.setCommandValue(alarmArmed ? "ON" : "OFF");
        command.setStatus(CommandStatus.PENDING);

        commandRepository.save(command);

        // Chuẩn bị dữ liệu push
        DeviceCommandResponse response = commandMapper.toResponse(command);
        DeviceTelemetryResponse latestStateResponse = deviceMapper.toTelemetryResponse(device, latestState);

        // Đăng ký push sau khi transaction commit xong
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                log.info("[WS] Pushing ALARM command and Latest State to device {} after commit", deviceCode);
                // 1. Gửi lệnh cho ESP32
                webSocketService.pushCommandToDevice(deviceCode, response);
                // 2. Gửi trạng thái mới cho App
                webSocketService.pushLatestStateToApp(deviceCode, latestStateResponse);
            }
        });

        return response;
    }

    @Transactional(readOnly = true)
    public List<DeviceCommandResponse> getPendingCommands(Long deviceId) {
        List<DeviceCommand> commands = commandRepository.findByDeviceIdAndStatus(deviceId, CommandStatus.PENDING);
        return commandMapper.toResponseList(commands);
    }

    @Transactional
    public void markAsSent(Long commandId) {
        DeviceCommand command = commandRepository.findById(commandId)
                .orElseThrow(() -> new ResourceNotFoundException(DeviceErrorCode.RESOURCE_NOT_FOUND));
        command.setStatus(CommandStatus.SENT);
        commandRepository.save(command);
    }

    @Transactional
    public void markAsAcknowledged(Long commandId) {
        DeviceCommand command = commandRepository.findById(commandId)
                .orElseThrow(() -> new ResourceNotFoundException(DeviceErrorCode.RESOURCE_NOT_FOUND));
        command.setStatus(CommandStatus.ACKNOWLEDGED);
        command.setExecutedAt(LocalDateTime.now());
        commandRepository.save(command);
    }
    @Transactional
    public DeviceCommandResponse createDisconnectCommand(String deviceCode, Long userId) {
        Device device = deviceRepository.findByDeviceCode(deviceCode)
                .orElseThrow(() -> new ResourceNotFoundException(DeviceErrorCode.DEVICE_NOT_FOUND));

        if (device.getOwnerUserId() == null || !device.getOwnerUserId().equals(userId)) {
            throw new BadRequestException(DeviceErrorCode.ACCESS_DENIED);
        }

        // Tạo command DISCONNECT_DEVICE
        DeviceCommand command = new DeviceCommand();
        command.setDevice(device);
        command.setCommandType(CommandType.DISCONNECT_DEVICE);
        command.setCommandValue("DISCONNECT");
        command.setStatus(CommandStatus.PENDING);

        commandRepository.save(command);

        // Tạo response và ĐẨY SAU KHI TRANSACTION COMMIT
        DeviceCommandResponse response = commandMapper.toResponse(command);

        // Đăng ký push sau khi transaction commit xong
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                log.info("[WS] Pushing DISCONNECT command to device {} after commit", deviceCode);
                webSocketService.pushCommandToDevice(deviceCode, response);
            }
        });

        return response;
    }
}
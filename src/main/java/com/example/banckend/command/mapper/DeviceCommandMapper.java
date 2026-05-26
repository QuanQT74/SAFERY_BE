package com.example.banckend.command.mapper;

import com.example.banckend.command.dto.request.DeviceCommandRequest;
import com.example.banckend.command.dto.response.DeviceCommandResponse;
import com.example.banckend.command.entity.DeviceCommand;
import com.example.banckend.device.entity.Device;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DeviceCommandMapper {

    public DeviceCommand toEntity(Device device, DeviceCommandRequest request) {
        DeviceCommand command = new DeviceCommand();
        command.setDevice(device);
        command.setCommandType(request.getCommandType());
        command.setCommandValue(request.getCommandValue());
        return command;
    }

    public DeviceCommandResponse toResponse(DeviceCommand command) {
        return DeviceCommandResponse.builder()
                .id(command.getId())
                .deviceId(command.getDevice().getId())
                .deviceName(command.getDevice().getDeviceName())
                .commandType(command.getCommandType())
                .commandValue(command.getCommandValue())
                .status(command.getStatus())
                .executedAt(command.getExecutedAt())
                .createdAt(command.getCreatedAt())
                .build();
    }

    public List<DeviceCommandResponse> toResponseList(List<DeviceCommand> commands) {
        return commands.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}

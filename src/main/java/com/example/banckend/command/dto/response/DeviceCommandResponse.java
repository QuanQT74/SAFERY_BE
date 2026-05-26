package com.example.banckend.command.dto.response;

import com.example.banckend.conmon.enums.CommandStatus;
import com.example.banckend.conmon.enums.CommandType;
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
public class DeviceCommandResponse {

    private Long id;
    private Long deviceId;
    private String deviceName;
    private CommandType commandType;
    private String commandValue;
    private CommandStatus status;
    private LocalDateTime executedAt;
    private LocalDateTime createdAt;
}

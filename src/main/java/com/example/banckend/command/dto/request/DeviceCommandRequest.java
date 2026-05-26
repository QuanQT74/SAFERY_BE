package com.example.banckend.command.dto.request;

import com.example.banckend.conmon.enums.CommandType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceCommandRequest {

    @NotNull(message = "Command type is required")
    private CommandType commandType;

    private String commandValue;
}

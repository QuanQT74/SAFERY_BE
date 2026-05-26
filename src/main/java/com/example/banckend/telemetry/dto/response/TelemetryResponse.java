package com.example.banckend.telemetry.dto.response;

import com.example.banckend.command.dto.response.DeviceCommandResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryResponse {

    private Boolean success;
    private List<DeviceCommandResponse> pendingCommands;
    private String nickname;
}
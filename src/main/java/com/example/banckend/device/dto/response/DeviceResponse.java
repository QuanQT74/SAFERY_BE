package com.example.banckend.device.dto.response;

import com.example.banckend.conmon.enums.DeviceStatus;
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
public class DeviceResponse {

    private Long id;
    private String deviceCode;
    private String deviceName;
    private String nickname;
    private DeviceStatus status;
    private Boolean online;
    private LocalDateTime lastSeenAt;
}

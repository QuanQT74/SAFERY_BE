package com.example.banckend.device.dto.request;
import jakarta.validation.constraints.NotBlank;
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
public class RegisterDeviceRequest {

    @NotBlank(message = "Device code is required")
    private String hardwareId;

    @NotBlank(message = "Device name is required")
    private String deviceName;

    private String nickname;

    @NotBlank(message = "ESP32 serial is required")
    private String esp32Serial;

    private String firmwareVersion;
}

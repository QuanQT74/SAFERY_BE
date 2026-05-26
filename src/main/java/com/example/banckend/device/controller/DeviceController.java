package com.example.banckend.device.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.banckend.auth.service.JwtService;
import com.example.banckend.command.dto.response.DeviceCommandResponse;
import com.example.banckend.command.service.DeviceCommandService;
import com.example.banckend.conmon.response.ApiResponse;
import com.example.banckend.device.dto.request.PairDeviceRequest;
import com.example.banckend.device.dto.request.FanCommandRequest;
import com.example.banckend.device.dto.request.AlarmCommandRequest;
import com.example.banckend.device.dto.request.UpdateNicknameRequest;
import com.example.banckend.device.dto.response.DeviceResponse;
import com.example.banckend.device.dto.response.DeviceTelemetryResponse;
import com.example.banckend.device.dto.response.PairDeviceResponse;
import com.example.banckend.device.service.DeviceRegistrationService;
import com.example.banckend.telemetry.dto.response.SensorAggregatedResponse;
import com.example.banckend.telemetry.dto.response.SensorStatsResponse;
import com.example.banckend.telemetry.dto.response.TelemetryStatsResponse;
import com.example.banckend.telemetry.service.TelemetryHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Device Management", description = "APIs for device management")
@SecurityRequirement(name = "bearerAuth")
public class DeviceController {

    private final DeviceRegistrationService deviceRegistrationService;
    private final DeviceCommandService deviceCommandService;
    private final TelemetryHistoryService telemetryHistoryService;
    private final JwtService jwtService;

    @PostMapping("/devices/pair-and-provision")
    public ResponseEntity<ApiResponse<PairDeviceResponse>> pairAndProvision(
            @Valid @RequestBody PairDeviceRequest request,
            @RequestHeader("Authorization") String authorizationHeader) {

        String accessToken = extractToken(authorizationHeader);
        Long userId = Long.parseLong(jwtService.extractUserId(accessToken));

        PairDeviceResponse response = deviceRegistrationService.pairAndProvision(request, userId);
        return ResponseEntity.ok(ApiResponse.success("Device paired successfully", response));
    }

    @GetMapping("/devices")
    public ResponseEntity<ApiResponse<List<DeviceResponse>>> getMyDevices(
            @RequestHeader("Authorization") String authorizationHeader) {

        String accessToken = extractToken(authorizationHeader);
        Long userId = Long.parseLong(jwtService.extractUserId(accessToken));

        List<DeviceResponse> devices = deviceRegistrationService.getDevicesByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Devices fetched successfully", devices));
    }

    @GetMapping("/devices/{deviceCode}/telemetry")
    public ResponseEntity<ApiResponse<DeviceTelemetryResponse>> getTelemetry(
            @PathVariable String deviceCode,
            @RequestHeader("Authorization") String authorizationHeader) {

        String accessToken = extractToken(authorizationHeader);
        Long userId = Long.parseLong(jwtService.extractUserId(accessToken));

        DeviceTelemetryResponse response = deviceRegistrationService.getTelemetryResponse(deviceCode, userId);
        return ResponseEntity.ok(ApiResponse.success("Telemetry fetched successfully", response));
    }

    @GetMapping("/devices/{deviceCode}/telemetry/stats")
    public ResponseEntity<ApiResponse<TelemetryStatsResponse>> getTelemetryStats(
            @PathVariable String deviceCode,
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(defaultValue = "24h") String period) {

        String accessToken = extractToken(authorizationHeader);
        Long userId = Long.parseLong(jwtService.extractUserId(accessToken));

        TelemetryStatsResponse stats = telemetryHistoryService.getTelemetryStats(deviceCode, userId, period);
        return ResponseEntity.ok(ApiResponse.success("Telemetry stats fetched", stats));
    }

    @GetMapping("/devices/{deviceCode}/sensor/{sensor}/aggregated")
    public ResponseEntity<ApiResponse<List<SensorAggregatedResponse>>> getSensorAggregated(
            @PathVariable String deviceCode,
            @PathVariable String sensor,
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(defaultValue = "24h") String period) {

        String accessToken = extractToken(authorizationHeader);
        Long userId = Long.parseLong(jwtService.extractUserId(accessToken));

        List<SensorAggregatedResponse> data = telemetryHistoryService.getAggregatedSensor(deviceCode, userId, sensor, period);
        return ResponseEntity.ok(ApiResponse.success("Aggregated " + sensor + " data", data));
    }

    @GetMapping("/devices/{deviceCode}/sensor/{sensor}/summary")
    public ResponseEntity<ApiResponse<SensorStatsResponse>> getSensorRaw(
            @PathVariable String deviceCode,
            @PathVariable String sensor,
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(defaultValue = "24h") String period) {

        String accessToken = extractToken(authorizationHeader);
        Long userId = Long.parseLong(jwtService.extractUserId(accessToken));

        SensorStatsResponse data = telemetryHistoryService.getRawSensorStats(deviceCode, userId, sensor, period);
        return ResponseEntity.ok(ApiResponse.success("Sensor stats for " + sensor, data));
    }


    @PostMapping("/devices/{deviceCode}/fan")
    public ResponseEntity<ApiResponse<DeviceCommandResponse>> controlFan(
            @PathVariable String deviceCode,
            @Valid @RequestBody FanCommandRequest request,
            @RequestHeader("Authorization") String authorizationHeader) {

        String accessToken = extractToken(authorizationHeader);
        Long userId = Long.parseLong(jwtService.extractUserId(accessToken));

        DeviceCommandResponse response = deviceCommandService.createFanCommand(deviceCode, request.getFanStatus(), userId);
        return ResponseEntity.ok(ApiResponse.success("Fan command sent successfully", response));
    }

    @PostMapping("/devices/{deviceCode}/alarm")
    public ResponseEntity<ApiResponse<DeviceCommandResponse>> controlAlarm(
            @PathVariable String deviceCode,
            @Valid @RequestBody AlarmCommandRequest request,
            @RequestHeader("Authorization") String authorizationHeader) {

        String accessToken = extractToken(authorizationHeader);
        Long userId = Long.parseLong(jwtService.extractUserId(accessToken));

        DeviceCommandResponse response = deviceCommandService.createAlarmCommand(deviceCode, request.getAlarmArmed(), userId);
        return ResponseEntity.ok(ApiResponse.success("Alarm command sent successfully", response));
    }

    @PatchMapping("/devices/{deviceCode}/nickname")
    public ResponseEntity<ApiResponse<Void>> updateNickname(
            @PathVariable String deviceCode,
            @Valid @RequestBody UpdateNicknameRequest request,
            @RequestHeader("Authorization") String authorizationHeader) {

        String accessToken = extractToken(authorizationHeader);
        Long userId = Long.parseLong(jwtService.extractUserId(accessToken));

        deviceRegistrationService.updateNickname(deviceCode, userId, request.getNickname());
        return ResponseEntity.ok(ApiResponse.success("Nickname updated successfully", null));
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Authorization header is invalid");
        }
        return authorizationHeader.substring(7);
    }
    @PostMapping("/devices/{deviceCode}/disconnect")
    public ResponseEntity<ApiResponse<Void>> disconnectDevice(@PathVariable String deviceCode, @RequestHeader("Authorization") String authorizationHeader) {
        String accessToken = extractToken(authorizationHeader);
        Long userId = Long.parseLong(jwtService.extractUserId(accessToken));

        deviceRegistrationService.DisconnectDevice(deviceCode, userId);
        return ResponseEntity.ok(ApiResponse.success("Device disconnected successfully", null));
    }
}

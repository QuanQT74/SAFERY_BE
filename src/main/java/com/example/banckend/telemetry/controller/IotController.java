package com.example.banckend.telemetry.controller;
import com.example.banckend.conmon.response.ApiResponse;
import com.example.banckend.telemetry.dto.request.TelemetryRequest;
import com.example.banckend.telemetry.dto.response.TelemetryResponse;
import com.example.banckend.telemetry.service.TelemetryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/api/iot")
@RequiredArgsConstructor
@Tag(name = "IoT Telemetry", description = "APIs for device telemetry (no auth required)")
public class IotController {
    @PostMapping("/pair-status")
    public ResponseEntity<ApiResponse<Void>> pairStatus(@RequestBody java.util.Map<String, Object> request) {
        // Dummy endpoint for old ESP32 firmware
        return ResponseEntity.ok(ApiResponse.success("Pair status check", null));
    }

    private final TelemetryService telemetryService;

    @PostMapping("/telemetry")
    public ResponseEntity<ApiResponse<TelemetryResponse>> receiveTelemetry(
            @Valid @RequestBody TelemetryRequest request) {

        TelemetryResponse response = telemetryService.processTelemetry(request);
        return ResponseEntity.ok(ApiResponse.success("Telemetry received", response));
    }

    @PostMapping("/command-ack")
    public ResponseEntity<ApiResponse<Void>> acknowledgeCommand(
            @RequestBody Map<String, Object> request) {

        String deviceCode = (String) request.get("deviceCode");
        String deviceSecret = (String) request.get("deviceSecret");
        Long commandId = Long.valueOf(request.get("commandId").toString());

        telemetryService.acknowledgeCommand(deviceCode, deviceSecret, commandId);
        return ResponseEntity.ok(ApiResponse.success("Command acknowledged", null));
    }
}

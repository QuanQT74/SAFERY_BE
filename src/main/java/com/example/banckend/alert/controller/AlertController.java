package com.example.banckend.alert.controller;

import com.example.banckend.alert.dto.response.AlertResponse;
import com.example.banckend.auth.service.JwtService;
import com.example.banckend.conmon.response.ApiResponse;
import com.example.banckend.alert.service.AlertService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Alert Management", description = "APIs for sensor alerts")
@SecurityRequirement(name = "bearerAuth")
public class AlertController {

    private final AlertService alertService;
    private final JwtService jwtService;

    // Lấy alerts có phân trang và filter (20 bản ghi mới nhất)
    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<Page<AlertResponse>>> getAlerts(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "ALL") String severity,
            @RequestParam(defaultValue = "ALL") String status) {

        Long userId = extractUserId(authorizationHeader);
        Pageable pageable = PageRequest.of(page, size);
        Page<AlertResponse> alerts = alertService.getFilteredAlerts(userId, severity, status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Alerts fetched successfully", alerts));
    }

    // Đánh dấu alert đã đọc/đã xử lý
    @PutMapping("/alerts/{alertId}/acknowledge")
    public ResponseEntity<ApiResponse<AlertResponse>> acknowledgeAlert(
            @PathVariable Long alertId,
            @RequestHeader("Authorization") String authorizationHeader) {

        Long userId = extractUserId(authorizationHeader);
        AlertResponse alert = alertService.acknowledgeAlert(alertId, userId);
        return ResponseEntity.ok(ApiResponse.success("Alert acknowledged", alert));
    }

    // Xóa alert
    @DeleteMapping("/alerts/{alertId}")
    public ResponseEntity<ApiResponse<Void>> deleteAlert(
            @PathVariable Long alertId,
            @RequestHeader("Authorization") String authorizationHeader) {

        Long userId = extractUserId(authorizationHeader);
        alertService.deleteAlert(alertId, userId);
        return ResponseEntity.ok(ApiResponse.success("Alert deleted", null));
    }

    // Lấy số lượng alert chưa đọc
    @GetMapping("/alerts/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @RequestHeader("Authorization") String authorizationHeader) {

        Long userId = extractUserId(authorizationHeader);
        long count = alertService.countUnreadByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Unread alert count", count));
    }

    private Long extractUserId(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Authorization header is invalid");
        }
        String token = authorizationHeader.substring(7);
        return Long.parseLong(jwtService.extractUserId(token));
    }
}

package com.example.banckend.conmon.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum DeviceErrorCode {
    BAD_REQUEST(20001, HttpStatus.BAD_REQUEST, "Thiết bị chưa sẵn sàng kết nối. Vui lòng nhấn giữ nút Reset trên thiết bị để vào chế độ Pairing."),
    RESOURCE_NOT_FOUND(20002, HttpStatus.NOT_FOUND, "Resource not found"),
    DEVICE_NOT_FOUND(20100, HttpStatus.NOT_FOUND, "Device not found"),
    DEVICE_ALREADY_EXISTS(20101, HttpStatus.CONFLICT, "Device already exists"),
    INVALID_DEVICE_SECRET(20102, HttpStatus.UNAUTHORIZED, "Invalid device secret"),
    HARDWARE_ID_NOT_FOUND(20103, HttpStatus.NOT_FOUND, "Hardware ID not found"),
    TELEMETRY_SAVE_FAILED(20200, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save telemetry"),
    LATEST_STATE_UPDATE_FAILED(20201, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update latest state"),
    INVALID_DEVICE_CODE(20104, HttpStatus.BAD_REQUEST, "Invalid device code"),
    DEVICE_ALREADY_PAIRED(20105, HttpStatus.CONFLICT, "Device already paired with another user"),
    ACCESS_DENIED(20106, HttpStatus.FORBIDDEN, "Access denied"),
    UNAUTHORIZED(20107, HttpStatus.UNAUTHORIZED, "Unauthorized");

    private final int code;
    private final HttpStatus status;
    private final String messageTemplate;

    DeviceErrorCode(int code, HttpStatus status, String messageTemplate) {
        this.code = code;
        this.status = status;
        this.messageTemplate = messageTemplate;
    }
}

package com.example.banckend.conmon.exception;

import com.example.banckend.conmon.enums.DeviceErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ResourceNotFoundException extends RuntimeException {

    private final int code;
    private final HttpStatus status;

    public ResourceNotFoundException(DeviceErrorCode errorCode) {
        super(errorCode.getMessageTemplate());
        this.code = errorCode.getCode();
        this.status = errorCode.getStatus();
    }

    public ResourceNotFoundException(String message) {
        super(message);
        this.code = 20002;
        this.status = HttpStatus.NOT_FOUND;
    }
}

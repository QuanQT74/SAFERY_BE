package com.example.banckend.conmon.exception;

import com.example.banckend.conmon.enums.DeviceErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BadRequestException extends RuntimeException {

    private final int code;
    private final HttpStatus status;

    public BadRequestException(DeviceErrorCode errorCode) {
        super(errorCode.getMessageTemplate());
        this.code = errorCode.getCode();
        this.status = errorCode.getStatus();
    }

    public BadRequestException(String message) {
        super(message);
        this.code = 20001;
        this.status = HttpStatus.BAD_REQUEST;
    }
}

package com.pantrymate.notification.devicetoken.domain.exception;

import com.pantrymate.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum DeviceTokenErrorCode implements ErrorCode {
    DEVICE_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "DEVICE-TOKEN-INVALID", "FCM 토큰이 비어있습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    DeviceTokenErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}

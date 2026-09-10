package com.pantrymate.pantryrecipe.pantry.domain.exception;

import com.pantrymate.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum PantryErrorCode implements ErrorCode {
    PANTRY_INVALID_NAME(HttpStatus.BAD_REQUEST, "PANTRY-INVALID-NAME", "식재료명은 1자 이상 20자 이하여야 합니다."),
    PANTRY_INVALID_DATE(HttpStatus.BAD_REQUEST, "PANTRY-INVALID-DATE", "올바른 날짜 형식(YYYY-MM-DD)을 입력해주세요."),
    PANTRY_INVALID_STORAGE(
            HttpStatus.BAD_REQUEST, "PANTRY-INVALID-STORAGE", "올바른 보관방법(REFRIGERATED, FROZEN, ROOM_TEMP)을 선택해주세요.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    PantryErrorCode(HttpStatus status, String code, String message) {
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

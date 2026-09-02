package com.pantrymate.product.category.domain.exception;

import com.pantrymate.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CategoryErrorCode implements ErrorCode {
    // --- 공통 에러 (COM) ---
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COM-40001", "잘못된 입력값입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}

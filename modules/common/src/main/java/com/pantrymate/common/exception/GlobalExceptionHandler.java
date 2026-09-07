package com.pantrymate.common.exception;

import com.pantrymate.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
            .status(errorCode.getStatus())
            .body(ApiResponse.error(errorCode));
    }

    // ****codeRabbit이 알려준 문제점에 대한 보완.****//
    /* JSON이 변환은 되었지만, 검증(@Valid) 단계에서 문제가 발생하였을 때,
    ex) 재고는 0개 미만으로 떨어지면 안되는데 값이 -5 등 정해진 범위에서 벗어날 때, 검증
    */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
        MethodArgumentNotValidException e) {
        return ResponseEntity
            .status(CommonErrorCode.INVALID_INPUT.getStatus())
            .body(ApiResponse.error(CommonErrorCode.INVALID_INPUT));
    }

    /* JSON 값이 이상하게 들어갈 때,
    ex) 정수가 들어가야하는 부분에 문자열이 오는 경우 등 타입 변환 자체가 실패할 때 검증
    */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
        HttpMessageNotReadableException e
    ) {
        return ResponseEntity
            .status(CommonErrorCode.INVALID_INPUT.getStatus())
            .body(ApiResponse.error(CommonErrorCode.INVALID_INPUT));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        return ResponseEntity
            .status(CommonErrorCode.INTERNAL_SERVER_ERROR.getStatus())
            .body(ApiResponse.error(CommonErrorCode.INTERNAL_SERVER_ERROR));
    }
}

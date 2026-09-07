package com.pantrymate.common.dto;

import com.pantrymate.common.exception.ErrorCode;
import java.time.LocalDateTime;

public record ApiResponse<T>(
    String status,      // "SUCCESS" 또는 "ERROR"
    String message,
    T data,
    String error,       // 에러코드 (성공 시 null)
    LocalDateTime timestamp
) {


    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("SUCCESS", message, data, null, LocalDateTime.now());
    }


    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>("SUCCESS", message, null, null, LocalDateTime.now());
    }


    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return new ApiResponse<>("ERROR", message, null, errorCode, LocalDateTime.now());
    }

    public static ApiResponse<Void> error(ErrorCode errorCode) {
        return new ApiResponse<>("ERROR", errorCode.getMessage(), null, errorCode.getCode(), LocalDateTime.now());
    }
}


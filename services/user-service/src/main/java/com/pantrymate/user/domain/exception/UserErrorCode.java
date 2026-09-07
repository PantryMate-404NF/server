package com.pantrymate.user.domain.exception;

import com.pantrymate.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum UserErrorCode implements ErrorCode {
    AUTH_INVALID_PROVIDER(HttpStatus.BAD_REQUEST, "AUTH-INVALID-PROVIDER", "지원하지 않는 소셜 로그인 제공자입니다."),
    AUTH_INVALID_AUTH_CODE(HttpStatus.BAD_REQUEST, "AUTH-INVALID-AUTH-CODE", "소셜 인가 코드가 유효하지 않습니다."),
    AUTH_OAUTH_COMMUNICATION_ERROR(
            HttpStatus.SERVICE_UNAVAILABLE, "AUTH-OAUTH-COMMUNICATION-ERROR", "소셜 인증 서버와의 통신 중 오류가 발생했습니다."),
    AUTH_MISSING_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-MISSING-TOKEN", "로그인이 필요합니다."),
    AUTH_EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-EXPIRED-TOKEN", "유효하지 않거나 만료된 리프레시 토큰입니다."),
    AUTH_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-INVALID-TOKEN", "인증 토큰이 유효하지 않습니다."),

    USER_NOTFOUND_ID(HttpStatus.NOT_FOUND, "USER-NOTFOUND-ID", "존재하지 않는 회원입니다."),
    USER_INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "USER-INVALID-NICKNAME", "닉네임은 2자 이상 20자 이하로 입력해 주세요."),

    ONBOARD_INVALID_INPUT(HttpStatus.BAD_REQUEST, "ONBOARD-INVALID-INPUT", "가족 구성원 수는 1명 이상이어야 합니다."),
    ONBOARD_NOTFOUND_PREFERENCE(HttpStatus.NOT_FOUND, "ONBOARD-NOTFOUND-PREFERENCE", "온보딩 설정 정보를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    UserErrorCode(HttpStatus status, String code, String message) {
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

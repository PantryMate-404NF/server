package com.pantrymate.common.dto;

/**
 * Gateway가 JWT 검증 후 X-User-Id 헤더로 내려주는 사용자 정보.
 * 각 서비스는 이 헤더를 신뢰하고, 토큰을 다시 검증하지 않는다.
 */
public record CurrentUser(Long userId) {
}

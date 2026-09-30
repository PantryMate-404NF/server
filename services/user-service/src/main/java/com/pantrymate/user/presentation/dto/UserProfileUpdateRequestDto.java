package com.pantrymate.user.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public record UserProfileUpdateRequestDto(
        @Schema(description = "2~20자. 생략(null)하면 기존 값 유지", example = "수현", nullable = true) String nickname,
        @Schema(description = "생략(null)하면 기존 값 유지", example = "https://k.kakaocdn.net/profile.jpg", nullable = true)
                String profileImageUrl,
        @Schema(description = "숫자만 10~11자리(010으로 시작). 생략(null)하면 기존 값 유지", example = "01012345678", nullable = true)
                String phoneNumber,
        @Schema(description = "생략(null)하면 기존 값 유지. 미래 날짜 불가", example = "1999-01-31", nullable = true)
                LocalDate birthDate) {}

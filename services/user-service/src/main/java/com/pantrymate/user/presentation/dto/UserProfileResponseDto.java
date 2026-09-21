package com.pantrymate.user.presentation.dto;

import com.pantrymate.user.domain.AuthProvider;
import com.pantrymate.user.domain.User;
import com.pantrymate.user.domain.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

public record UserProfileResponseDto(
        @Schema(description = "JS Number 정밀도 문제를 피하기 위해 문자열로 반환", example = "1024", requiredMode = Schema.RequiredMode.REQUIRED)
                String userId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) AuthProvider provider,
        @Schema(description = "소셜 제공자가 이메일 동의를 안 받으면 null", example = "user@example.com", nullable = true)
                String email,
        @Schema(example = "수현", requiredMode = Schema.RequiredMode.REQUIRED) String nickname,
        @Schema(
                        description = "소셜 프로필 이미지가 없으면 null",
                        example = "https://k.kakaocdn.net/profile.jpg",
                        nullable = true)
                String profileImageUrl,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UserRole role,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean onboardingCompleted,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) OffsetDateTime createdAt,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) OffsetDateTime updatedAt) {

    public static UserProfileResponseDto of(User user, boolean onboardingCompleted) {
        return new UserProfileResponseDto(
                String.valueOf(user.getUserId()),
                user.getProvider(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getRole(),
                onboardingCompleted,
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}

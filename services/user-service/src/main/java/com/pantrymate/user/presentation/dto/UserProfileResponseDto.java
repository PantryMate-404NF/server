package com.pantrymate.user.presentation.dto;

import com.pantrymate.user.domain.AuthProvider;
import com.pantrymate.user.domain.User;
import com.pantrymate.user.domain.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

public record UserProfileResponseDto(
        String userId,
        AuthProvider provider,
        @Schema(description = "소셜 제공자가 이메일 동의를 안 받으면 null", nullable = true) String email,
        String nickname,
        @Schema(description = "소셜 프로필 이미지가 없으면 null", nullable = true) String profileImageUrl,
        UserRole role,
        boolean onboardingCompleted,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {

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

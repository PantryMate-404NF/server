package com.pantrymate.user.presentation.dto;

import com.pantrymate.user.domain.User;
import java.time.OffsetDateTime;

public record UserProfileResponseDto(
        Long userId,
        String provider,
        String email,
        String nickname,
        String profileImageUrl,
        String role,
        boolean onboardingCompleted,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {

    public static UserProfileResponseDto of(User user, boolean onboardingCompleted) {
        return new UserProfileResponseDto(
                user.getUserId(),
                user.getProvider().name(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getRole().name(),
                onboardingCompleted,
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}

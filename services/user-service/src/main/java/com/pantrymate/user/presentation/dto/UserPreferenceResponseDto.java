package com.pantrymate.user.presentation.dto;

import com.pantrymate.user.domain.UserPreference;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;

public record UserPreferenceResponseDto(
        String preferenceId,
        String userId,
        Integer familyMemberCount,
        @Schema(description = "온보딩 전이거나 값을 저장한 적 없으면 null", nullable = true) List<String> preferredFoodTypes,
        @Schema(description = "온보딩 전이거나 값을 저장한 적 없으면 null", nullable = true) List<String> allergies,
        boolean onboardingCompleted,
        Integer onboardingStep,
        OffsetDateTime updatedAt) {

    public static UserPreferenceResponseDto from(UserPreference preference) {
        return new UserPreferenceResponseDto(
                String.valueOf(preference.getPreferenceId()),
                String.valueOf(preference.getUserId()),
                preference.getFamilyMemberCount(),
                preference.getPreferredFoodTypes(),
                preference.getAllergies(),
                preference.isOnboardingCompleted(),
                preference.getOnboardingStep(),
                preference.getUpdatedAt());
    }
}

package com.pantrymate.user.presentation.dto;

import com.pantrymate.user.domain.UserPreference;
import java.time.OffsetDateTime;
import java.util.List;

public record UserPreferenceResponseDto(
        Long preferenceId,
        Long userId,
        Integer familyMemberCount,
        List<String> preferredFoodTypes,
        List<String> allergies,
        boolean onboardingCompleted,
        Integer onboardingStep,
        OffsetDateTime updatedAt) {

    public static UserPreferenceResponseDto from(UserPreference preference) {
        return new UserPreferenceResponseDto(
                preference.getPreferenceId(),
                preference.getUserId(),
                preference.getFamilyMemberCount(),
                preference.getPreferredFoodTypes(),
                preference.getAllergies(),
                preference.isOnboardingCompleted(),
                preference.getOnboardingStep(),
                preference.getUpdatedAt());
    }
}

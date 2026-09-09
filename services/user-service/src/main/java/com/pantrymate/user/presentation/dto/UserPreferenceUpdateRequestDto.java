package com.pantrymate.user.presentation.dto;

import java.util.List;

public record UserPreferenceUpdateRequestDto(
        Integer familyMemberCount,
        List<String> preferredFoodTypes,
        List<String> allergies,
        Boolean onboardingCompleted,
        Integer onboardingStep) {
}

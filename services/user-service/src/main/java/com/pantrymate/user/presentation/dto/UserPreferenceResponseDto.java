package com.pantrymate.user.presentation.dto;

import com.pantrymate.user.domain.UserPreference;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;

public record UserPreferenceResponseDto(
        @Schema(description = "JS Number 정밀도 문제를 피하기 위해 문자열로 반환", example = "501", requiredMode = Schema.RequiredMode.REQUIRED)
                String preferenceId,
        @Schema(example = "1024", requiredMode = Schema.RequiredMode.REQUIRED) String userId,
        @Schema(example = "3", requiredMode = Schema.RequiredMode.REQUIRED) Integer familyMemberCount,
        @Schema(
                        description = "온보딩 전이거나 값을 저장한 적 없으면 null",
                        allowableValues = {"KOREAN", "WESTERN", "JAPANESE", "CHINESE", "ETC"},
                        nullable = true)
                List<String> preferredFoodTypes,
        @Schema(description = "온보딩 전이거나 값을 저장한 적 없으면 null", example = "[\"갑각류\", \"견과류\"]", nullable = true)
                List<String> allergies,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean onboardingCompleted,
        @Schema(example = "2", requiredMode = Schema.RequiredMode.REQUIRED) Integer onboardingStep,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) OffsetDateTime updatedAt) {

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

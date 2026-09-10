package com.pantrymate.user.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record TastePreferenceDto(
        @Schema(description = "짠맛 선호도 (1~5 단계)", example = "3", minimum = "1", maximum = "5") Integer salty,
        @Schema(description = "단맛 선호도 (1~5 단계)", example = "3", minimum = "1", maximum = "5") Integer sweet,
        @Schema(description = "매운맛 선호도 (1~5 단계)", example = "3", minimum = "1", maximum = "5") Integer spicy) {
}

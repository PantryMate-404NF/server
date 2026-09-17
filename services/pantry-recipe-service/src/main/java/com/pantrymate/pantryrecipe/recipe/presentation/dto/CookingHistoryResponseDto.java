package com.pantrymate.pantryrecipe.recipe.presentation.dto;

import com.pantrymate.pantryrecipe.recipe.domain.CookingHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

public record CookingHistoryResponseDto(
        @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED) Long historyId,
        @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED) Long recipeId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) OffsetDateTime cookedAt) {

    public static CookingHistoryResponseDto from(CookingHistory history) {
        return new CookingHistoryResponseDto(
                history.getHistoryId(), history.getRecipe().getRecipeId(), history.getCookedAt());
    }
}

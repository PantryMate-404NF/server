package com.pantrymate.pantryrecipe.recipe.presentation.dto;

import com.pantrymate.pantryrecipe.recipe.domain.CookingHistory;
import java.time.OffsetDateTime;

public record CookingHistoryResponseDto(Long historyId, Long recipeId, OffsetDateTime cookedAt) {

    public static CookingHistoryResponseDto from(CookingHistory history) {
        return new CookingHistoryResponseDto(
                history.getHistoryId(), history.getRecipe().getRecipeId(), history.getCookedAt());
    }
}

package com.pantrymate.pantryrecipe.recipe.presentation.dto;

import com.pantrymate.pantryrecipe.pantry.domain.PantryItem;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public record RecipeFilterIngredientResponseDto(
        @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED) Long ingredientId,
        @Schema(example = "계란", requiredMode = Schema.RequiredMode.REQUIRED) String name,
        @Schema(example = "2026-09-20", requiredMode = Schema.RequiredMode.REQUIRED) LocalDate expiryDate,
        @Schema(description = "소비기한 경과 여부", requiredMode = Schema.RequiredMode.REQUIRED) boolean expired,
        @Schema(description = "기본 선택 여부(소비기한 임박순 최대 3개)", requiredMode = Schema.RequiredMode.REQUIRED)
                boolean defaultSelected) {

    public static RecipeFilterIngredientResponseDto of(PantryItem representative, boolean defaultSelected) {
        boolean expired = representative.getExpiryDate().isBefore(LocalDate.now());
        return new RecipeFilterIngredientResponseDto(
                representative.getIngredient().getIngredientId(),
                representative.getIngredient().getName(),
                representative.getExpiryDate(),
                expired,
                defaultSelected);
    }
}

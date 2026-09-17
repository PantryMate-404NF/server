package com.pantrymate.pantryrecipe.recipe.presentation.dto;

import com.pantrymate.pantryrecipe.recipe.domain.RecipeIngredient;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

public record RecipeIngredientResponseDto(
        @Schema(example = "10", requiredMode = Schema.RequiredMode.REQUIRED) Long ingredientId,
        @Schema(example = "계란", requiredMode = Schema.RequiredMode.REQUIRED) String name,
        @Schema(example = "https://cdn.pantrymate.com/ingredients/10.jpg", nullable = true) String imageUrl,
        @Schema(example = "2", nullable = true) BigDecimal requiredAmount,
        @Schema(example = "개", nullable = true) String unit,
        @Schema(description = "주재료 여부", requiredMode = Schema.RequiredMode.REQUIRED) boolean isMain) {

    public static RecipeIngredientResponseDto from(RecipeIngredient recipeIngredient) {
        return new RecipeIngredientResponseDto(
                recipeIngredient.getIngredient().getIngredientId(),
                recipeIngredient.getName(),
                recipeIngredient.getIngredient().getImageUrl(),
                recipeIngredient.getRequiredAmount(),
                recipeIngredient.getUnit(),
                recipeIngredient.isMain());
    }
}

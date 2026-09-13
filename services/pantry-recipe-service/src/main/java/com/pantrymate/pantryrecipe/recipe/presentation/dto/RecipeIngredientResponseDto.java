package com.pantrymate.pantryrecipe.recipe.presentation.dto;

import com.pantrymate.pantryrecipe.recipe.domain.RecipeIngredient;
import java.math.BigDecimal;

public record RecipeIngredientResponseDto(
        Long ingredientId, String name, BigDecimal requiredAmount, String unit, boolean isMain) {

    public static RecipeIngredientResponseDto from(RecipeIngredient recipeIngredient) {
        return new RecipeIngredientResponseDto(
                recipeIngredient.getIngredient().getIngredientId(),
                recipeIngredient.getName(),
                recipeIngredient.getRequiredAmount(),
                recipeIngredient.getUnit(),
                recipeIngredient.isMain());
    }
}

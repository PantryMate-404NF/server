package com.pantrymate.pantryrecipe.recipe.presentation.dto;

import com.pantrymate.pantryrecipe.recipe.domain.Recipe;
import com.pantrymate.pantryrecipe.recipe.domain.enums.CuisineType;
import com.pantrymate.pantryrecipe.recipe.domain.enums.RecipeDifficulty;
import java.util.List;

public record RecipeDetailResponseDto(
        Long recipeId,
        String title,
        String description,
        CuisineType cuisineType,
        Integer cookingTime,
        Integer servings,
        RecipeDifficulty difficulty,
        String thumbnailUrl,
        List<RecipeStepResponseDto> steps,
        List<RecipeIngredientResponseDto> ingredients) {

    public static RecipeDetailResponseDto of(
            Recipe recipe, List<RecipeStepResponseDto> steps, List<RecipeIngredientResponseDto> ingredients) {
        return new RecipeDetailResponseDto(
                recipe.getRecipeId(),
                recipe.getTitle(),
                recipe.getDescription(),
                recipe.getCuisineType(),
                recipe.getCookingTime(),
                recipe.getServings(),
                recipe.getDifficulty(),
                recipe.getThumbnailUrl(),
                steps,
                ingredients);
    }
}

package com.pantrymate.pantryrecipe.recipe.presentation.dto;

import com.pantrymate.pantryrecipe.recipe.domain.Recipe;
import com.pantrymate.pantryrecipe.recipe.domain.enums.CuisineType;
import com.pantrymate.pantryrecipe.recipe.domain.enums.RecipeDifficulty;

public record RecipeResponseDto(
        Long recipeId,
        String title,
        String description,
        CuisineType cuisineType,
        Integer cookingTime,
        Integer servings,
        RecipeDifficulty difficulty,
        String thumbnailUrl) {

    public static RecipeResponseDto from(Recipe recipe) {
        return new RecipeResponseDto(
                recipe.getRecipeId(),
                recipe.getTitle(),
                recipe.getDescription(),
                recipe.getCuisineType(),
                recipe.getCookingTime(),
                recipe.getServings(),
                recipe.getDifficulty(),
                recipe.getThumbnailUrl());
    }
}

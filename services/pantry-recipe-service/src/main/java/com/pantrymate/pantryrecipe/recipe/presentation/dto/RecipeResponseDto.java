package com.pantrymate.pantryrecipe.recipe.presentation.dto;

import com.pantrymate.pantryrecipe.recipe.domain.Recipe;
import com.pantrymate.pantryrecipe.recipe.domain.enums.CuisineType;
import com.pantrymate.pantryrecipe.recipe.domain.enums.RecipeDifficulty;
import io.swagger.v3.oas.annotations.media.Schema;

public record RecipeResponseDto(
        @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED) Long recipeId,
        @Schema(example = "계란볶음밥", requiredMode = Schema.RequiredMode.REQUIRED) String title,
        @Schema(example = "간단하게 만드는 계란볶음밥", requiredMode = Schema.RequiredMode.REQUIRED) String description,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) CuisineType cuisineType,
        @Schema(description = "예상 조리 시간(분)", example = "15", requiredMode = Schema.RequiredMode.REQUIRED)
                Integer cookingTime,
        @Schema(description = "기준 인분 수", example = "1", requiredMode = Schema.RequiredMode.REQUIRED) Integer servings,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) RecipeDifficulty difficulty,
        @Schema(example = "https://cdn.pantrymate.com/recipes/1/thumb.jpg", nullable = true) String thumbnailUrl) {

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

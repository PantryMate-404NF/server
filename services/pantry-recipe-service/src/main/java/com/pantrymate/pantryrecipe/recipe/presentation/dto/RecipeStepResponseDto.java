package com.pantrymate.pantryrecipe.recipe.presentation.dto;

import com.pantrymate.pantryrecipe.recipe.domain.RecipeStep;
import io.swagger.v3.oas.annotations.media.Schema;

public record RecipeStepResponseDto(
        @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED) Integer stepNumber,
        @Schema(example = "계란을 풀어 소금 간을 한다.", requiredMode = Schema.RequiredMode.REQUIRED) String description,
        @Schema(example = "https://cdn.pantrymate.com/recipes/1/steps/1.jpg", nullable = true) String imageUrl) {

    public static RecipeStepResponseDto from(RecipeStep step) {
        return new RecipeStepResponseDto(step.getStepNumber(), step.getDescription(), step.getImageUrl());
    }
}

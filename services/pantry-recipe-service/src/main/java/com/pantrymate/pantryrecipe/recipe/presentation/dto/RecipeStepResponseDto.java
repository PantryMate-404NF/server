package com.pantrymate.pantryrecipe.recipe.presentation.dto;

import com.pantrymate.pantryrecipe.recipe.domain.RecipeStep;

public record RecipeStepResponseDto(Integer stepNumber, String description, String imageUrl) {

    public static RecipeStepResponseDto from(RecipeStep step) {
        return new RecipeStepResponseDto(step.getStepNumber(), step.getDescription(), step.getImageUrl());
    }
}

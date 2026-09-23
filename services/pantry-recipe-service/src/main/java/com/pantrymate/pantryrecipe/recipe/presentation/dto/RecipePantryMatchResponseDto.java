package com.pantrymate.pantryrecipe.recipe.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record RecipePantryMatchResponseDto(
        @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED) Long recipeId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<RecipeIngredientPantryMatchResponseDto> ingredients) {}

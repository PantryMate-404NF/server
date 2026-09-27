package com.pantrymate.pantryrecipe.recipe.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.data.domain.Page;

public record RecipeListResponseDto(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<RecipeResponseDto> content,
        @Schema(example = "21491", requiredMode = Schema.RequiredMode.REQUIRED) long totalElements,
        @Schema(example = "1075", requiredMode = Schema.RequiredMode.REQUIRED) int totalPages) {

    public static RecipeListResponseDto from(Page<RecipeResponseDto> page) {
        return new RecipeListResponseDto(page.getContent(), page.getTotalElements(), page.getTotalPages());
    }
}

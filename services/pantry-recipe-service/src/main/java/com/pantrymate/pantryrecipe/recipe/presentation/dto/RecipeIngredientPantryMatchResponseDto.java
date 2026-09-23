package com.pantrymate.pantryrecipe.recipe.presentation.dto;

import com.pantrymate.pantryrecipe.pantry.domain.PantryItem;
import com.pantrymate.pantryrecipe.pantry.domain.enums.PantryExpiryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public record RecipeIngredientPantryMatchResponseDto(
        @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED) Long ingredientId,
        @Schema(example = "마늘", requiredMode = Schema.RequiredMode.REQUIRED) String name,
        @Schema(description = "보유 여부. matchedPantryItems가 1개 이상이면 true", requiredMode = Schema.RequiredMode.REQUIRED)
                boolean hasIngredient,
        @Schema(description = "동일 식재료 ID로 매칭되는 팬트리 항목. 동일 재료가 여러 건이면 모두 포함")
                List<MatchedPantryItemResponseDto> matchedPantryItems) {

    public record MatchedPantryItemResponseDto(
            @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED) Long pantryItemId,
            @Schema(example = "2026-09-20", requiredMode = Schema.RequiredMode.REQUIRED) LocalDate expiryDate,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) PantryExpiryStatus expiryStatus) {

        public static MatchedPantryItemResponseDto from(PantryItem pantryItem) {
            long dDay = ChronoUnit.DAYS.between(LocalDate.now(), pantryItem.getExpiryDate());
            return new MatchedPantryItemResponseDto(
                    pantryItem.getPantryItemId(),
                    pantryItem.getExpiryDate(),
                    PantryExpiryStatus.fromRemainingDays(dDay));
        }
    }
}

package com.pantrymate.pantryrecipe.ai.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pantrymate.pantryrecipe.ingredient.domain.Ingredient;
import java.util.List;

public record AiIngredientListResponseDto(List<Item> items) {

    public record Item(
            @JsonProperty("ingredient_id") Long ingredientId,
            String name,
            String category,
            @JsonProperty("default_storage_type") String defaultStorageType,
            @JsonProperty("default_shelf_life_days") Integer defaultShelfLifeDays,
            @JsonProperty("extended_consumption_days") Integer extendedConsumptionDays,
            @JsonProperty("is_staple") @JsonInclude(JsonInclude.Include.NON_NULL) Boolean staple,
            @JsonInclude(JsonInclude.Include.NON_NULL) List<String> allergens) {

        public static Item of(Ingredient ingredient, boolean includeCuratedFields) {
            return new Item(
                    ingredient.getIngredientId(),
                    ingredient.getName(),
                    ingredient.getCategory(),
                    ingredient.getDefaultStorageType() == null
                            ? null
                            : ingredient.getDefaultStorageType().name(),
                    ingredient.getDefaultShelfLifeDays(),
                    ingredient.getExtendedConsumptionDays(),
                    includeCuratedFields ? ingredient.isStaple() : null,
                    includeCuratedFields ? List.copyOf(ingredient.getAllergens()) : null);
        }
    }
}

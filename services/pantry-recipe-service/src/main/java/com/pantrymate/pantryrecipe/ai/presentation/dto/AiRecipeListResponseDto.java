package com.pantrymate.pantryrecipe.ai.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

public record AiRecipeListResponseDto(
        List<Item> items, @JsonProperty("next_cursor") String nextCursor, long total) {

    public record Item(
            @JsonProperty("recipe_id") Long recipeId,
            String title,
            @JsonProperty("cuisine_type") String cuisineType,
            @JsonProperty("cooking_time") Integer cookingTime,
            Integer servings,
            String difficulty,
            @JsonProperty("is_published") boolean published,
            @JsonProperty("updated_at") Instant updatedAt,
            List<IngredientItem> ingredients,
            Popularity popularity,
            Rating rating) {}

    public record IngredientItem(
            @JsonProperty("ingredient_id") Long ingredientId,
            String name,
            @JsonProperty("is_main") boolean main,
            String unit) {}

    public record Popularity(
            @JsonProperty("view_count") long viewCount,
            @JsonProperty("scrap_count") long scrapCount,
            @JsonProperty("order_count") long orderCount) {}

    public record Rating(Double average, long count) {}
}

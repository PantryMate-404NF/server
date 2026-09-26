package com.pantrymate.pantryrecipe.recipe.domain;

public record ProductCandidate(
        Long ingredientId,
        Long productId,
        String name,
        Long price,
        String thumbnailUrl,
        String unit,
        Integer capacity,
        Integer packageCount) {}

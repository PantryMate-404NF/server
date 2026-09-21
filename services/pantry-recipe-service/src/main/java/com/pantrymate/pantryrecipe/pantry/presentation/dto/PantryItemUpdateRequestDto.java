package com.pantrymate.pantryrecipe.pantry.presentation.dto;

public record PantryItemUpdateRequestDto(
        String ingredientName,
        String expiryDate,
        String sellByDate,
        String storageType,
        String imageUrl,
        Boolean cookable) {}

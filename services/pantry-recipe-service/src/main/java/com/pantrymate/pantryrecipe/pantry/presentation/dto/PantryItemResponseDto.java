package com.pantrymate.pantryrecipe.pantry.presentation.dto;

import com.pantrymate.pantryrecipe.ingredient.domain.enums.StorageType;
import com.pantrymate.pantryrecipe.pantry.domain.PantryItem;
import com.pantrymate.pantryrecipe.pantry.domain.enums.PantryRegisterType;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record PantryItemResponseDto(
        Long pantryItemId,
        String ingredientName,
        LocalDate expiryDate,
        long dDay,
        boolean isImminent,
        StorageType storageType,
        boolean isExpiryAutoCalculated,
        PantryRegisterType registerType,
        String imageUrl) {

    private static final long IMMINENT_THRESHOLD_DAYS = 3;

    public static PantryItemResponseDto from(PantryItem pantryItem) {
        long dDay = ChronoUnit.DAYS.between(LocalDate.now(), pantryItem.getExpiryDate());
        return new PantryItemResponseDto(
                pantryItem.getPantryItemId(),
                pantryItem.getName(),
                pantryItem.getExpiryDate(),
                dDay,
                dDay < IMMINENT_THRESHOLD_DAYS,
                pantryItem.getStorageType(),
                pantryItem.isExpiryAutoCalculated(),
                pantryItem.getRegisterType(),
                pantryItem.getImageUrl());
    }
}

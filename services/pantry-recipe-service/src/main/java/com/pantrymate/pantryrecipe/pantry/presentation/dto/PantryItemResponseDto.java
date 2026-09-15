package com.pantrymate.pantryrecipe.pantry.presentation.dto;

import com.pantrymate.pantryrecipe.ingredient.domain.enums.StorageType;
import com.pantrymate.pantryrecipe.pantry.domain.PantryItem;
import com.pantrymate.pantryrecipe.pantry.domain.enums.PantryExpiryStatus;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record PantryItemResponseDto(
        Long pantryItemId,
        String ingredientName,
        LocalDate expiryDate,
        long dDay,
        PantryExpiryStatus expiryStatus,
        StorageType storageType,
        boolean isExpiryAutoCalculated,
        String registerType,
        String imageUrl) {

    private static final long IMMINENT_THRESHOLD_DAYS = 3;
    private static final int NAME_DISPLAY_MAX_LENGTH = 10;

    public static PantryItemResponseDto from(PantryItem pantryItem) {
        long dDay = ChronoUnit.DAYS.between(LocalDate.now(), pantryItem.getExpiryDate());
        return new PantryItemResponseDto(
                pantryItem.getPantryItemId(),
                truncateName(pantryItem.getName()),
                pantryItem.getExpiryDate(),
                dDay,
                resolveExpiryStatus(dDay),
                pantryItem.getStorageType(),
                pantryItem.isExpiryAutoCalculated(),
                pantryItem.getRegisterType().getLabel(),
                pantryItem.getImageUrl());
    }

    private static PantryExpiryStatus resolveExpiryStatus(long dDay) {
        if (dDay < 0) {
            return PantryExpiryStatus.EXPIRED;
        }
        if (dDay < IMMINENT_THRESHOLD_DAYS) {
            return PantryExpiryStatus.IMMINENT;
        }
        return PantryExpiryStatus.NORMAL;
    }

    private static String truncateName(String name) {
        if (name.length() <= NAME_DISPLAY_MAX_LENGTH) {
            return name;
        }
        return name.substring(0, NAME_DISPLAY_MAX_LENGTH) + "…";
    }
}

package com.pantrymate.pantryrecipe.pantry.presentation.dto;

import com.pantrymate.pantryrecipe.ingredient.domain.enums.StorageType;
import com.pantrymate.pantryrecipe.pantry.domain.PantryItem;
import com.pantrymate.pantryrecipe.pantry.domain.enums.PantryExpiryStatus;
import com.pantrymate.pantryrecipe.pantry.domain.enums.PantryRegisterType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record PantryItemResponseDto(
        @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED) Long pantryItemId,
        @Schema(description = "식재료 사전 매칭 ID. 매칭 실패 시 null", example = "1", nullable = true) Long ingredientId,
        @Schema(example = "양파", requiredMode = Schema.RequiredMode.REQUIRED) String ingredientName,
        @Schema(description = "유통기한. 없으면 null", example = "2026-09-14", nullable = true) LocalDate sellByDate,
        @Schema(example = "2026-09-20", requiredMode = Schema.RequiredMode.REQUIRED) LocalDate expiryDate,
        @Schema(description = "소비기한까지 남은 일수 (음수면 경과)", example = "4", requiredMode = Schema.RequiredMode.REQUIRED)
                long dDay,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) PantryExpiryStatus expiryStatus,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) StorageType storageType,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean isExpiryAutoCalculated,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean isCookable,
        @Schema(
                        description = "등록 방식. MANUAL: 사용자 등록 / AUTO: 자사몰 구매 자동 등록",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                PantryRegisterType registerType,
        @Schema(description = "식재료 이미지 URL. 없으면 null", example = "https://cdn.pantrymate.com/pantry-items/1.jpg", nullable = true)
                String imageUrl) {

    private static final long IMMINENT_THRESHOLD_DAYS = 3;
    private static final int NAME_DISPLAY_MAX_LENGTH = 10;

    public static PantryItemResponseDto from(PantryItem pantryItem) {
        long dDay = ChronoUnit.DAYS.between(LocalDate.now(), pantryItem.getExpiryDate());
        return new PantryItemResponseDto(
                pantryItem.getPantryItemId(),
                pantryItem.getIngredient() == null ? null : pantryItem.getIngredient().getIngredientId(),
                truncateName(pantryItem.getName()),
                pantryItem.getSellByDate(),
                pantryItem.getExpiryDate(),
                dDay,
                resolveExpiryStatus(dDay),
                pantryItem.getStorageType(),
                pantryItem.isExpiryAutoCalculated(),
                pantryItem.isCookable(),
                pantryItem.getRegisterType(),
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

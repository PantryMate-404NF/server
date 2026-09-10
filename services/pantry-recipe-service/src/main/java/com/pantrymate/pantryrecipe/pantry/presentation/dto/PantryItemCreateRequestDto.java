package com.pantrymate.pantryrecipe.pantry.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record PantryItemCreateRequestDto(
        @Schema(
                        description = "식재료명 (1자 이상 20자 이하)",
                        example = "양파",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                String ingredientName,
        @Schema(description = "소비기한(YYYY-MM-DD). 미입력 시 등록일 기준으로 자동 계산됨", example = "2026-09-20")
                String expiryDate,
        @Schema(
                        description = "보관방법",
                        example = "REFRIGERATED",
                        allowableValues = {"REFRIGERATED", "FROZEN", "ROOM_TEMP"},
                        requiredMode = Schema.RequiredMode.REQUIRED)
                String storageType,
        @Schema(description = "식재료 이미지 URL") String imageUrl) {
}

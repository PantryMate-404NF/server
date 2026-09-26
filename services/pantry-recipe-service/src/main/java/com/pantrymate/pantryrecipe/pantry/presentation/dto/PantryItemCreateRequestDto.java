package com.pantrymate.pantryrecipe.pantry.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record PantryItemCreateRequestDto(
        @Schema(
                        description = "식재료명 (1자 이상 20자 이하)",
                        example = "양파",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                String ingredientName,
        @Schema(
                        description = "소비기한(YYYY-MM-DD). sellByDate와 함께 입력 시 우선 적용됨. 둘 다 미입력 시 등록일 기준으로 자동 계산됨",
                        example = "2026-09-20")
                String expiryDate,
        @Schema(
                        description = "유통기한(YYYY-MM-DD). expiryDate 미입력 시에만 사용되며, 이 값을 기준으로 소비기한이 산정됨",
                        example = "2026-09-14")
                String sellByDate,
        @Schema(
                        description = "보관방법",
                        example = "REFRIGERATED",
                        allowableValues = {"REFRIGERATED", "FROZEN", "ROOM_TEMP"},
                        requiredMode = Schema.RequiredMode.REQUIRED)
                String storageType,
        @Schema(description = "식재료 이미지 URL") String imageUrl,
        @Schema(
                        description = "구매일(YYYY-MM-DD). 영수증 사진 등록 시 OCR로 읽은 구매일을 넣는다. "
                                + "유통기한·소비기한이 모두 없으면 이 날짜(없으면 등록일)를 기준으로 소비기한이 자동 계산됨",
                        example = "2026-09-25",
                        nullable = true)
                String purchaseDate) {
}

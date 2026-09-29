package com.pantrymate.pantryrecipe.pantry.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record PantryImageUploadResponseDto(
        @Schema(
                        description = "업로드된 이미지 URL. 이 값을 그대로 POST/PATCH /api/pantry-items의 imageUrl에 넣는다",
                        example = "http://localhost:8080/api/pantry-items/images/3f1a9c2e-....jpg",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                String imageUrl) {}

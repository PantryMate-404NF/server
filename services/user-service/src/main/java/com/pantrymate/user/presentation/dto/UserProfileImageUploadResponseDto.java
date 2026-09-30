package com.pantrymate.user.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserProfileImageUploadResponseDto(
        @Schema(
                        description = "업로드된 이미지 URL. 이 값을 그대로 PATCH /api/users/me의 profileImageUrl에 넣는다",
                        example = "https://pantry-mate-items-542119828072.s3.ap-northeast-2.amazonaws.com/profile-images/3f1a9c2e-....jpg",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                String imageUrl) {}

package com.pantrymate.pantryrecipe.recipe.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record CookingCompleteRequestDto(
        @Schema(description = "조리 완료 후 팬트리에서 함께 삭제할 항목 ID 목록. 미입력 시 팬트리는 정리하지 않고 조리 이력만 기록")
                List<Long> pantryItemIds) {}

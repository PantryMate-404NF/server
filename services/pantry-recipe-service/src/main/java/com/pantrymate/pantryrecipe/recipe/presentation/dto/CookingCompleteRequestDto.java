package com.pantrymate.pantryrecipe.recipe.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record CookingCompleteRequestDto(
        @Schema(description = "조리 완료 후 팬트리에서 함께 삭제할 항목 ID 목록. 미입력 시 팬트리는 정리하지 않고 조리 이력만 기록")
                List<Long> pantryItemIds,
        @Schema(description = "추천 목록에서 들어온 경우 추천 응답의 requestId. AI 추천 학습용", nullable = true)
                String requestId,
        @Schema(description = "추천 목록에서의 순위(1부터). requestId와 함께 전달", nullable = true) Integer position) {}

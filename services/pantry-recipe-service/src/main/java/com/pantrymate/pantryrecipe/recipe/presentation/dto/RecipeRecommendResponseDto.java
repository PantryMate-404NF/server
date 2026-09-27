package com.pantrymate.pantryrecipe.recipe.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record RecipeRecommendResponseDto(
        @Schema(
                        description = "AI 추천 요청 ID. 이후 행동 이벤트(조회·스크랩·조리)를 보낼 때 함께 전달한다. 폴백(POPULARITY)이면 null",
                        nullable = true)
                String requestId,
        @Schema(description = "AI: AI 개인화 추천 / POPULARITY: AI를 쓸 수 없어 스크랩 수 기준 인기순으로 대체", example = "AI")
                String source,
        @Schema(description = "화면에 보이는 순서대로 정렬되어 있다. 순서를 바꾸지 말 것") List<Item> items) {

    @Schema(name = "RecipeRecommendItem")
    public record Item(
            @Schema(description = "노출 순위(1부터)", example = "1") int rank,
            @Schema(description = "추천 이유 한 문장(AI 제공). 폴백이면 null", nullable = true) String reason,
            @Schema(description = "필수 재료 충족률 0~1. 폴백이면 null", nullable = true) Double coverage,
            @Schema(description = "부족한 필수 재료 수. 폴백이면 null", nullable = true) Integer missingCount,
            @Schema(description = "부족한 필수 재료. 장보기 연결에 사용") List<MissingIngredient> missingIngredients,
            RecipeResponseDto recipe) {}

    public record MissingIngredient(
            @Schema(example = "37") Long ingredientId, @Schema(example = "소고기") String name) {}
}

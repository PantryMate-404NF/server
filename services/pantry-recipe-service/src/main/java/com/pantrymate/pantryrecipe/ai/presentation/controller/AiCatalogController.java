package com.pantrymate.pantryrecipe.ai.presentation.controller;

import com.pantrymate.common.exception.BusinessException;
import com.pantrymate.common.exception.CommonErrorCode;
import com.pantrymate.pantryrecipe.ai.application.AiCatalogService;
import com.pantrymate.pantryrecipe.ai.presentation.dto.AiIngredientListResponseDto;
import com.pantrymate.pantryrecipe.ai.presentation.dto.AiRecipeListResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI-INTERNAL", description = "AI 추천 서버 전용 조회 API (X-Internal-Api-Key 필요, gateway 미노출)")
@RestController
@RequestMapping("/internal/ai")
public class AiCatalogController {

    private final AiCatalogService aiCatalogService;

    public AiCatalogController(AiCatalogService aiCatalogService) {
        this.aiCatalogService = aiCatalogService;
    }

    @Operation(
            summary = "AI용 재료 사전 조회",
            description = "공통 응답 래퍼 없이 최상위 items를 반환한다. 인증: X-Internal-Api-Key 헤더")
    @GetMapping("/ingredients")
    public AiIngredientListResponseDto ingredients() {
        return aiCatalogService.getIngredients();
    }

    @Operation(
            summary = "AI용 레시피 조회",
            description = "공통 응답 래퍼 없이 최상위 items/next_cursor/total을 반환한다. recipe_id 오름차순 커서 페이지네이션이며 "
                    + "limit은 기본 1000, 최대 5000이다. updated_after를 주면 그 이후 수정된 레시피(비공개 포함)만 반환한다. "
                    + "인증: X-Internal-Api-Key 헤더")
    @GetMapping("/recipes")
    public AiRecipeListResponseDto recipes(
            @Parameter(description = "증분 동기화 기준 시각(ISO-8601, 시간대 포함)", example = "2026-09-21T00:00:00Z")
                    @RequestParam(name = "updated_after", required = false)
                    String updatedAfter,
            @Parameter(description = "이전 응답의 next_cursor. 첫 요청은 비움") @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지 크기(기본 1000, 최대 5000)") @RequestParam(required = false) Integer limit) {
        return aiCatalogService.getRecipes(parseUpdatedAfter(updatedAfter), cursor, limit);
    }

    private OffsetDateTime parseUpdatedAfter(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(raw.trim());
        } catch (DateTimeParseException e) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT);
        }
    }
}

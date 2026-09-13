package com.pantrymate.pantryrecipe.recipe.presentation.controller;

import com.pantrymate.common.dto.ApiResponse;
import com.pantrymate.pantryrecipe.recipe.application.RecipeService;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.RecipeResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "RECIPE", description = "레시피 추천 및 상세")
@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @Operation(summary = "레시피 추천 목록 조회", description = "초기 버전은 개인화 없이 공개된 DB 기본/큐레이션 레시피를 반환한다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<RecipeResponseDto>>> list() {
        List<RecipeResponseDto> response = recipeService.getAll();
        return ResponseEntity.ok(ApiResponse.success("레시피 목록 조회가 완료되었습니다.", response));
    }
}

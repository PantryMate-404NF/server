package com.pantrymate.pantryrecipe.recipe.presentation.controller;

import com.pantrymate.common.dto.ApiResponse;
import com.pantrymate.common.dto.CurrentUser;
import com.pantrymate.pantryrecipe.recipe.application.CookingHistoryService;
import com.pantrymate.pantryrecipe.recipe.application.RecipeScrapService;
import com.pantrymate.pantryrecipe.recipe.application.RecipeService;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.CookingCompleteRequestDto;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.CookingHistoryResponseDto;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.RecipeDetailResponseDto;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.RecipeResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "RECIPE", description = "레시피 추천 및 상세")
@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;
    private final RecipeScrapService recipeScrapService;
    private final CookingHistoryService cookingHistoryService;

    public RecipeController(
            RecipeService recipeService,
            RecipeScrapService recipeScrapService,
            CookingHistoryService cookingHistoryService) {
        this.recipeService = recipeService;
        this.recipeScrapService = recipeScrapService;
        this.cookingHistoryService = cookingHistoryService;
    }

    @Operation(summary = "레시피 추천 목록 조회", description = "현재 개발상으로는 개인화 없이 공개된 DB 기본/큐레이션 레시피를 반환한다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<RecipeResponseDto>>> list() {
        List<RecipeResponseDto> response = recipeService.getAll();
        return ResponseEntity.ok(ApiResponse.success("레시피 목록 조회가 완료되었습니다.", response));
    }

    @Operation(
            summary = "레시피 상세 조회",
            description = "조리 순서와 필요 식재료 목록(식재료 이미지 포함)을 포함한 레시피 상세를 반환한다. "
                    + "팬트리 보유/부족 재료 판별 및 부족 재료 상품 매핑은 추후 지원 예정이다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "RECIPE-NOTFOUND-ID")
    })
    @GetMapping("/{recipeId}")
    public ResponseEntity<ApiResponse<RecipeDetailResponseDto>> detail(@PathVariable Long recipeId) {
        RecipeDetailResponseDto response = recipeService.getById(recipeId);
        return ResponseEntity.ok(ApiResponse.success("레시피 상세 조회가 완료되었습니다.", response));
    }

    @Operation(summary = "스크랩 레시피 목록 조회", description = "로그인한 유저가 스크랩한 레시피를 최근 스크랩순으로 조회한다.")
    @SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUTH-UNAUTHORIZED")
    })
    @GetMapping("/scraps")
    public ResponseEntity<ApiResponse<List<RecipeResponseDto>>> scrapList(
            @Parameter(hidden = true) CurrentUser currentUser) {
        List<RecipeResponseDto> response = recipeScrapService.getScraps(currentUser.userId());
        return ResponseEntity.ok(ApiResponse.success("스크랩 레시피 목록 조회가 완료되었습니다.", response));
    }

    @Operation(summary = "레시피 스크랩 등록", description = "이미 스크랩된 레시피면 아무 동작 없이 성공 처리한다.")
    @SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "등록 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUTH-UNAUTHORIZED"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "RECIPE-NOTFOUND-ID")
    })
    @PostMapping("/{recipeId}/scrap")
    public ResponseEntity<ApiResponse<Void>> scrap(
            @Parameter(hidden = true) CurrentUser currentUser, @PathVariable Long recipeId) {
        recipeScrapService.scrap(currentUser.userId(), recipeId);
        return ResponseEntity.ok(ApiResponse.success("레시피를 스크랩했습니다.", null));
    }

    @Operation(summary = "레시피 스크랩 해제", description = "스크랩돼 있지 않아도 아무 동작 없이 성공 처리한다.")
    @SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "해제 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUTH-UNAUTHORIZED")
    })
    @DeleteMapping("/{recipeId}/scrap")
    public ResponseEntity<ApiResponse<Void>> unscrap(
            @Parameter(hidden = true) CurrentUser currentUser, @PathVariable Long recipeId) {
        recipeScrapService.unscrap(currentUser.userId(), recipeId);
        return ResponseEntity.ok(ApiResponse.success("레시피 스크랩을 해제했습니다.", null));
    }

    @Operation(
            summary = "레시피 조리 완료",
            description = "조리 완료 이력을 저장한다. pantryItemIds를 함께 보내면 팬트리 항목을 삭제한다"
                    + "요청 바디 또는 pantryItemIds는 생략 가능하며, 이 경우 팬트리는 정리하지 않는다. "
                    + "정리 대상 팬트리 항목을 찾기 위한 필요 재료-팬트리 매칭 조회는 추후 지원 예정이다.")
    @SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조리 완료 처리 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUTH-UNAUTHORIZED"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404", description = "RECIPE-NOTFOUND-ID / PANTRY-NOTFOUND-ITEM")
    })
    @PostMapping("/{recipeId}/cook-complete")
    public ResponseEntity<ApiResponse<CookingHistoryResponseDto>> cookComplete(
            @Parameter(hidden = true) CurrentUser currentUser,
            @PathVariable Long recipeId,
            @RequestBody(required = false) CookingCompleteRequestDto request) {
        List<Long> pantryItemIds = request == null ? null : request.pantryItemIds();
        CookingHistoryResponseDto response =
                cookingHistoryService.complete(currentUser.userId(), recipeId, pantryItemIds);
        return ResponseEntity.ok(ApiResponse.success("조리 완료가 기록되었습니다.", response));
    }
}

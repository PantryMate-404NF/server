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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.MediaType;
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
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                                                        {
                                                          "status": "SUCCESS",
                                                          "message": "레시피 목록 조회가 완료되었습니다.",
                                                          "data": [
                                                            {
                                                              "recipeId": 1,
                                                              "title": "계란볶음밥",
                                                              "description": "간단하게 만드는 계란볶음밥",
                                                              "cuisineType": "KOREAN",
                                                              "cookingTime": 15,
                                                              "servings": 1,
                                                              "difficulty": "EASY",
                                                              "thumbnailUrl": "https://cdn.pantrymate.com/recipes/1/thumb.jpg"
                                                            }
                                                          ],
                                                          "error": null,
                                                          "timestamp": "2026-09-16T01:23:45.678Z"
                                                        }
                                                        """)))
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
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                                                        {
                                                          "status": "SUCCESS",
                                                          "message": "레시피 상세 조회가 완료되었습니다.",
                                                          "data": {
                                                            "recipeId": 1,
                                                            "title": "계란볶음밥",
                                                            "description": "간단하게 만드는 계란볶음밥",
                                                            "cuisineType": "KOREAN",
                                                            "cookingTime": 15,
                                                            "servings": 1,
                                                            "difficulty": "EASY",
                                                            "thumbnailUrl": "https://cdn.pantrymate.com/recipes/1/thumb.jpg",
                                                            "steps": [
                                                              {
                                                                "stepNumber": 1,
                                                                "description": "계란을 풀어 소금 간을 한다.",
                                                                "imageUrl": "https://cdn.pantrymate.com/recipes/1/steps/1.jpg"
                                                              }
                                                            ],
                                                            "ingredients": [
                                                              {
                                                                "ingredientId": 10,
                                                                "name": "계란",
                                                                "imageUrl": "https://cdn.pantrymate.com/ingredients/10.jpg",
                                                                "requiredAmount": 2,
                                                                "unit": "개",
                                                                "isMain": true
                                                              }
                                                            ]
                                                          },
                                                          "error": null,
                                                          "timestamp": "2026-09-16T01:23:45.678Z"
                                                        }
                                                        """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "RECIPE-NOTFOUND-ID",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                                                        {
                                                          "status": "ERROR",
                                                          "message": "해당 레시피를 찾을 수 없습니다.",
                                                          "data": null,
                                                          "error": "RECIPE-NOTFOUND-ID",
                                                          "timestamp": "2026-09-16T01:23:45.678Z"
                                                        }
                                                        """)))
    })
    @GetMapping("/{recipeId}")
    public ResponseEntity<ApiResponse<RecipeDetailResponseDto>> detail(@PathVariable Long recipeId) {
        RecipeDetailResponseDto response = recipeService.getById(recipeId);
        return ResponseEntity.ok(ApiResponse.success("레시피 상세 조회가 완료되었습니다.", response));
    }

    @Operation(summary = "스크랩 레시피 목록 조회", description = "로그인한 유저가 스크랩한 레시피를 최근 스크랩순으로 조회한다.")
    @SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                                                        {
                                                          "status": "SUCCESS",
                                                          "message": "스크랩 레시피 목록 조회가 완료되었습니다.",
                                                          "data": [
                                                            {
                                                              "recipeId": 1,
                                                              "title": "계란볶음밥",
                                                              "description": "간단하게 만드는 계란볶음밥",
                                                              "cuisineType": "KOREAN",
                                                              "cookingTime": 15,
                                                              "servings": 1,
                                                              "difficulty": "EASY",
                                                              "thumbnailUrl": "https://cdn.pantrymate.com/recipes/1/thumb.jpg"
                                                            }
                                                          ],
                                                          "error": null,
                                                          "timestamp": "2026-09-16T01:23:45.678Z"
                                                        }
                                                        """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "AUTH-UNAUTHORIZED",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                                                        {
                                                          "status": "ERROR",
                                                          "message": "인증 정보가 유효하지 않습니다.",
                                                          "data": null,
                                                          "error": "AUTH-UNAUTHORIZED",
                                                          "timestamp": "2026-09-16T01:23:45.678Z"
                                                        }
                                                        """)))
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
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "등록 성공",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                                                        {
                                                          "status": "SUCCESS",
                                                          "message": "레시피를 스크랩했습니다.",
                                                          "data": null,
                                                          "error": null,
                                                          "timestamp": "2026-09-16T01:23:45.678Z"
                                                        }
                                                        """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "AUTH-UNAUTHORIZED",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                                                        {
                                                          "status": "ERROR",
                                                          "message": "인증 정보가 유효하지 않습니다.",
                                                          "data": null,
                                                          "error": "AUTH-UNAUTHORIZED",
                                                          "timestamp": "2026-09-16T01:23:45.678Z"
                                                        }
                                                        """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "RECIPE-NOTFOUND-ID",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                                                        {
                                                          "status": "ERROR",
                                                          "message": "해당 레시피를 찾을 수 없습니다.",
                                                          "data": null,
                                                          "error": "RECIPE-NOTFOUND-ID",
                                                          "timestamp": "2026-09-16T01:23:45.678Z"
                                                        }
                                                        """)))
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
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "해제 성공",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                                                        {
                                                          "status": "SUCCESS",
                                                          "message": "레시피 스크랩을 해제했습니다.",
                                                          "data": null,
                                                          "error": null,
                                                          "timestamp": "2026-09-16T01:23:45.678Z"
                                                        }
                                                        """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "AUTH-UNAUTHORIZED",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                                                        {
                                                          "status": "ERROR",
                                                          "message": "인증 정보가 유효하지 않습니다.",
                                                          "data": null,
                                                          "error": "AUTH-UNAUTHORIZED",
                                                          "timestamp": "2026-09-16T01:23:45.678Z"
                                                        }
                                                        """)))
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
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "조리 완료 처리 성공",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                                                        {
                                                          "status": "SUCCESS",
                                                          "message": "조리 완료가 기록되었습니다.",
                                                          "data": {
                                                            "historyId": 1,
                                                            "recipeId": 1,
                                                            "cookedAt": "2026-09-16T01:23:45.678Z"
                                                          },
                                                          "error": null,
                                                          "timestamp": "2026-09-16T01:23:45.678Z"
                                                        }
                                                        """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "AUTH-UNAUTHORIZED",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                                                        {
                                                          "status": "ERROR",
                                                          "message": "인증 정보가 유효하지 않습니다.",
                                                          "data": null,
                                                          "error": "AUTH-UNAUTHORIZED",
                                                          "timestamp": "2026-09-16T01:23:45.678Z"
                                                        }
                                                        """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "RECIPE-NOTFOUND-ID / PANTRY-NOTFOUND-ITEM",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                examples = {
                                    @ExampleObject(
                                            name = "RECIPE-NOTFOUND-ID",
                                            value =
                                                    """
                                                    {
                                                      "status": "ERROR",
                                                      "message": "해당 레시피를 찾을 수 없습니다.",
                                                      "data": null,
                                                      "error": "RECIPE-NOTFOUND-ID",
                                                      "timestamp": "2026-09-16T01:23:45.678Z"
                                                    }
                                                    """),
                                    @ExampleObject(
                                            name = "PANTRY-NOTFOUND-ITEM",
                                            value =
                                                    """
                                                    {
                                                      "status": "ERROR",
                                                      "message": "해당 팬트리 식재료를 찾을 수 없습니다.",
                                                      "data": null,
                                                      "error": "PANTRY-NOTFOUND-ITEM",
                                                      "timestamp": "2026-09-16T01:23:45.678Z"
                                                    }
                                                    """)
                                }))
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

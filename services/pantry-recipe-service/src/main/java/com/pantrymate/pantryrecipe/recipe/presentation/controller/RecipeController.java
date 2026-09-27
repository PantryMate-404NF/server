package com.pantrymate.pantryrecipe.recipe.presentation.controller;

import com.pantrymate.common.dto.ApiResponse;
import com.pantrymate.common.dto.CurrentUser;
import com.pantrymate.common.exception.BusinessException;
import com.pantrymate.common.exception.CommonErrorCode;
import com.pantrymate.pantryrecipe.pantry.application.DeliveryAutoRegisterService;
import com.pantrymate.pantryrecipe.recipe.application.CookingHistoryService;
import com.pantrymate.pantryrecipe.recipe.application.RecipeRecommendService;
import com.pantrymate.pantryrecipe.recipe.application.RecipeScrapService;
import com.pantrymate.pantryrecipe.recipe.application.RecipeService;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.CookingCompleteRequestDto;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.CookingHistoryResponseDto;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.RecipeDetailResponseDto;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.RecipeFilterIngredientResponseDto;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.RecipeListResponseDto;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.RecipePantryMatchResponseDto;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.RecipeProductMatchResponseDto;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.RecipeRecommendResponseDto;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.RecipeResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "RECIPE", description = "레시피 추천 및 상세")
@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private static final int MAX_PAGE_SIZE = 100;

    private final RecipeService recipeService;
    private final RecipeScrapService recipeScrapService;
    private final CookingHistoryService cookingHistoryService;
    private final DeliveryAutoRegisterService deliveryAutoRegisterService;
    private final RecipeRecommendService recipeRecommendService;

    public RecipeController(
            RecipeService recipeService,
            RecipeScrapService recipeScrapService,
            CookingHistoryService cookingHistoryService,
            DeliveryAutoRegisterService deliveryAutoRegisterService,
            RecipeRecommendService recipeRecommendService) {
        this.recipeService = recipeService;
        this.recipeScrapService = recipeScrapService;
        this.cookingHistoryService = cookingHistoryService;
        this.deliveryAutoRegisterService = deliveryAutoRegisterService;
        this.recipeRecommendService = recipeRecommendService;
    }

    @Operation(
            summary = "개인화 레시피 추천",
            description = "AI 추천 서버가 사용자의 팬트리(요리가능 재료)와 알레르기를 반영해 추천한 레시피를 순위 순으로 반환한다. "
                    + "source=AI면 requestId를 이후 행동 이벤트에 함께 보내야 하고, AI를 쓸 수 없으면 source=POPULARITY(스크랩 수 기준, "
                    + "알레르기 재료 제외)로 대체된다. 알레르기 정보를 조회하지 못하면 안전을 위해 503을 반환한다.")
    @SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400", description = "COMMON-001(size 1~100, maxMinutes 1 이상)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUTH-UNAUTHORIZED"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "503", description = "RECIPE-UNAVAILABLE-RECOMMEND")
    })
    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<RecipeRecommendResponseDto>> recommendations(
            @Parameter(hidden = true) CurrentUser currentUser,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer maxMinutes) {
        if (size <= 0 || size > MAX_PAGE_SIZE || (maxMinutes != null && maxMinutes <= 0)) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT);
        }
        deliveryAutoRegisterService.syncUser(currentUser.userId());
        RecipeRecommendResponseDto response =
                recipeRecommendService.recommend(currentUser.userId(), size, maxMinutes);
        return ResponseEntity.ok(ApiResponse.success("추천 레시피 조회가 완료되었습니다.", response));
    }

    @Operation(
            summary = "레시피 추천 목록 조회",
            description = "현재 개발상으로는 개인화 없이 공개된 DB 기본/큐레이션 레시피를 페이지 단위로 반환한다. "
                    + "ingredientIds(최대 3개)를 전달하면 해당 식재료가 포함된 레시피를 매칭 개수순으로 우선 노출하고, "
                    + "나머지 레시피를 뒤이어 반환한다(빈 결과 없음).")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400", description = "RECIPE-INVALID-FILTER / COMMON-001(page 음수, size 1~100 범위 밖)")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<RecipeListResponseDto>> list(
            @RequestParam(required = false) List<Long> ingredientIds,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = toPageable(page, size);
        RecipeListResponseDto response = recipeService.getAll(ingredientIds, pageable);
        return ResponseEntity.ok(ApiResponse.success("레시피 목록 조회가 완료되었습니다.", response));
    }

    @Operation(
            summary = "레시피 필터용 팬트리 식재료 후보 조회",
            description = "로그인 유저의 팬트리 식재료 중 레시피 필요 재료와 매칭 가능한(ingredient_id가 있는) 항목을 "
                    + "식재료 단위로 반환한다. 소비기한이 지났어도 삭제되지 않았으면 포함하되 expired로 구분 표시하며, "
                    + "소비기한이 지나지 않은 식재료 중 잔여기간이 가장 짧은 최대 3개는 defaultSelected=true로 표시한다.")
    @SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUTH-UNAUTHORIZED")
    })
    @GetMapping("/filter-ingredients")
    public ResponseEntity<ApiResponse<List<RecipeFilterIngredientResponseDto>>> filterIngredients(
            @Parameter(hidden = true) CurrentUser currentUser) {
        deliveryAutoRegisterService.syncUser(currentUser.userId());
        List<RecipeFilterIngredientResponseDto> response = recipeService.getFilterIngredients(currentUser.userId());
        return ResponseEntity.ok(ApiResponse.success("필터 식재료 후보 조회가 완료되었습니다.", response));
    }

    @Operation(
            summary = "레시피 상세 조회",
            description = "조리 순서와 필요 식재료 목록(식재료 이미지 포함)을 포함한 레시피 상세를 반환한다. "
                    + "팬트리 보유 여부·매칭 항목은 인증이 필요한 GET /{recipeId}/pantry-match로 별도 조회한다. "
                    + "부족 재료 상품 매핑은 추후 지원 예정이다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "RECIPE-NOTFOUND-ID")
    })
    @GetMapping("/{recipeId}")
    public ResponseEntity<ApiResponse<RecipeDetailResponseDto>> detail(
            @PathVariable Long recipeId,
            @Parameter(hidden = true) @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @Parameter(description = "추천 목록에서 들어온 경우 추천 응답의 requestId(AI 학습용)") @RequestParam(required = false) String requestId,
            @Parameter(description = "추천 목록에서의 순위(1부터)") @RequestParam(required = false) Integer position) {
        RecipeDetailResponseDto response = recipeService.getById(recipeId, userId, requestId, position);
        return ResponseEntity.ok(ApiResponse.success("레시피 상세 조회가 완료되었습니다.", response));
    }

    @Operation(
            summary = "레시피 필요 재료 팬트리 매칭 조회",
            description = "레시피 필요 재료 각각에 대해 동일 식재료 ID로 매칭되는 로그인 유저의 팬트리 항목(pantryItemId)을 반환한다. "
                    + "동일 재료가 여러 건 등록돼 있으면 요리가능 여부와 무관하게 모두 반환하며, 조리완료 시 식재료 정리 대상 조회에도 사용한다. "
                    + "보유 여부(hasIngredient)는 요리가능 ON인 항목이 있을 때만 true다.")
    @SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUTH-UNAUTHORIZED"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "RECIPE-NOTFOUND-ID")
    })
    @GetMapping("/{recipeId}/pantry-match")
    public ResponseEntity<ApiResponse<RecipePantryMatchResponseDto>> pantryMatch(
            @Parameter(hidden = true) CurrentUser currentUser, @PathVariable Long recipeId) {
        deliveryAutoRegisterService.syncUser(currentUser.userId());
        RecipePantryMatchResponseDto response = recipeService.getPantryMatch(currentUser.userId(), recipeId);
        return ResponseEntity.ok(ApiResponse.success("팬트리 매칭 조회가 완료되었습니다.", response));
    }

    @Operation(
            summary = "레시피 필요 재료 상품 매칭 조회",
            description = "레시피 필요 재료별로 팬트리 보유 여부(요리가능 ON 기준)와 자사몰 대표 상품을 반환한다. hasIngredient=false인 재료가 부족 재료다. "
                    + "레시피 단위가 g이고 필요량이 있는 재료만 매칭하며, 같은 식재료의 판매중 상품 중 필요 용량 이상이면서 "
                    + "가장 근접한 용량의 상품(동률이면 최저가)을 고른다. 필요 용량을 채우는 상품이 없으면 가장 큰 용량 상품을 "
                    + "capacitySufficient=false로 반환한다. 그 외 단위는 UNSUPPORTED로 반환한다. "
                    + "장바구니에는 매칭된 상품을 수량 1개로 POST /api/cart/items에 담는다.")
    @SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUTH-UNAUTHORIZED"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "RECIPE-NOTFOUND-ID")
    })
    @GetMapping("/{recipeId}/product-match")
    public ResponseEntity<ApiResponse<RecipeProductMatchResponseDto>> productMatch(
            @Parameter(hidden = true) CurrentUser currentUser, @PathVariable Long recipeId) {
        deliveryAutoRegisterService.syncUser(currentUser.userId());
        RecipeProductMatchResponseDto response = recipeService.getProductMatch(currentUser.userId(), recipeId);
        return ResponseEntity.ok(ApiResponse.success("상품 매칭 조회가 완료되었습니다.", response));
    }

    @Operation(
            summary = "레시피 검색",
            description = "레시피명 또는 필요 식재료명에 검색어와 완전히 일치하는 단어가 있는 레시피를 페이지 단위로 조회한다("
                    + "형태소 분석 없는 단어 단위 풀텍스트 검색이라 부분 문자열은 매칭하지 않음, 예: '카레' 검색 시 "
                    + "'카레라이스'처럼 다른 단어의 일부로만 포함된 경우는 매칭되지 않음). 레시피명 매칭을 우선 노출하고 "
                    + "식재료명 매칭이 뒤따르며, 팬트리 보유 여부·개인화 순위는 반영하지 않는다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공(결과 없으면 빈 배열)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "RECIPE-INVALID-SEARCH-KEYWORD / COMMON-001(page 음수, size 1~100 범위 밖)")
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<RecipeListResponseDto>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = toPageable(page, size);
        RecipeListResponseDto response = recipeService.search(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success("레시피 검색이 완료되었습니다.", response));
    }

    private Pageable toPageable(int page, int size) {
        if (page < 0 || size <= 0 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT);
        }
        return PageRequest.of(page, size);
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
            @Parameter(hidden = true) CurrentUser currentUser,
            @PathVariable Long recipeId,
            @Parameter(description = "추천 목록에서 들어온 경우 추천 응답의 requestId(AI 학습용)") @RequestParam(required = false) String requestId,
            @Parameter(description = "추천 목록에서의 순위(1부터)") @RequestParam(required = false) Integer position) {
        recipeScrapService.scrap(currentUser.userId(), recipeId, requestId, position);
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
            @Parameter(hidden = true) CurrentUser currentUser,
            @PathVariable Long recipeId,
            @Parameter(description = "추천 목록에서 들어온 경우 추천 응답의 requestId(AI 학습용)") @RequestParam(required = false) String requestId,
            @Parameter(description = "추천 목록에서의 순위(1부터)") @RequestParam(required = false) Integer position) {
        recipeScrapService.unscrap(currentUser.userId(), recipeId, requestId, position);
        return ResponseEntity.ok(ApiResponse.success("레시피 스크랩을 해제했습니다.", null));
    }

    @Operation(
            summary = "레시피 조리 완료",
            description = "조리 완료 이력을 저장한다. GET /{recipeId}/pantry-match로 조회한 정리 대상 중 사용자가 선택한 "
                    + "pantryItemIds를 함께 보내면 이력 저장과 같은 트랜잭션에서 해당 팬트리 항목을 삭제한다. "
                    + "요청 바디 또는 pantryItemIds는 생략 가능하며, 이 경우 팬트리는 정리하지 않는다.")
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
        String requestId = request == null ? null : request.requestId();
        Integer position = request == null ? null : request.position();
        CookingHistoryResponseDto response =
                cookingHistoryService.complete(currentUser.userId(), recipeId, pantryItemIds, requestId, position);
        return ResponseEntity.ok(ApiResponse.success("조리 완료가 기록되었습니다.", response));
    }
}

package com.pantrymate.pantryrecipe.recipe.presentation.dto;

import com.pantrymate.pantryrecipe.recipe.domain.ProductCandidate;
import com.pantrymate.pantryrecipe.recipe.domain.enums.ProductMatchStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record RecipeProductMatchResponseDto(
        @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED) Long recipeId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<IngredientProductMatchResponseDto> ingredients) {

    public record IngredientProductMatchResponseDto(
            @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED) Long ingredientId,
            @Schema(example = "소고기", requiredMode = Schema.RequiredMode.REQUIRED) String name,
            @Schema(description = "팬트리 보유 여부. false인 재료가 부족 재료", requiredMode = Schema.RequiredMode.REQUIRED)
                    boolean hasIngredient,
            @Schema(
                            description = "MATCHED: 상품 매칭됨 / NO_PRODUCT: 구매 가능한 상품 없음 / "
                                    + "UNSUPPORTED: 레시피 단위가 g가 아니거나 필요량이 없어 매칭 대상 아님",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    ProductMatchStatus matchStatus,
            @Schema(description = "MATCHED일 때만 존재", nullable = true) MatchedProductResponseDto product) {}

    public record MatchedProductResponseDto(
            @Schema(example = "10", requiredMode = Schema.RequiredMode.REQUIRED) Long productId,
            @Schema(example = "한우 국거리 300g", requiredMode = Schema.RequiredMode.REQUIRED) String name,
            @Schema(example = "12900", requiredMode = Schema.RequiredMode.REQUIRED) Long price,
            @Schema(nullable = true) String thumbnailUrl,
            @Schema(example = "GRAM") String unit,
            @Schema(description = "구성 1개당 용량", example = "300") Integer capacity,
            @Schema(description = "구성 개수(예: 3개입 → 3). 미입력이면 null", nullable = true) Integer packageCount,
            @Schema(description = "상품 전체 용량(capacity × packageCount)이 레시피 필요량 이상인지. false면 단일 상품으로 필요량을 채울 수 없음(장바구니에서 수량 조정)")
                    boolean capacitySufficient) {

        public static MatchedProductResponseDto of(ProductCandidate product, boolean capacitySufficient) {
            return new MatchedProductResponseDto(
                    product.productId(),
                    product.name(),
                    product.price(),
                    product.thumbnailUrl(),
                    product.unit(),
                    product.capacity(),
                    product.packageCount(),
                    capacitySufficient);
        }
    }
}

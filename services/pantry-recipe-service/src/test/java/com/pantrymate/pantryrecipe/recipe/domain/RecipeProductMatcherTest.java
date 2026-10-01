package com.pantrymate.pantryrecipe.recipe.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.pantrymate.pantryrecipe.recipe.domain.RecipeProductMatcher.Result;
import com.pantrymate.pantryrecipe.recipe.domain.enums.ProductMatchStatus;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class RecipeProductMatcherTest {

    @Test
    void 레시피_단위가_g가_아니면_UNSUPPORTED를_반환한다() {
        List<ProductCandidate> candidates = List.of(candidate(1L, "GRAM", 500, null, 1000L));

        Result result = RecipeProductMatcher.match(BigDecimal.valueOf(100), "ml", candidates);

        assertThat(result.status()).isEqualTo(ProductMatchStatus.UNSUPPORTED);
        assertThat(result.product()).isNull();
    }

    @Test
    void 필요량이_null이면_UNSUPPORTED를_반환한다() {
        Result result = RecipeProductMatcher.match(null, "g", List.of());

        assertThat(result.status()).isEqualTo(ProductMatchStatus.UNSUPPORTED);
    }

    @Test
    void 필요량이_0_이하이면_UNSUPPORTED를_반환한다() {
        Result result = RecipeProductMatcher.match(BigDecimal.ZERO, "g", List.of());

        assertThat(result.status()).isEqualTo(ProductMatchStatus.UNSUPPORTED);
    }

    @Test
    void g_단위는_대소문자와_앞뒤_공백을_무시한다() {
        List<ProductCandidate> candidates = List.of(candidate(1L, "GRAM", 500, null, 1000L));

        Result result = RecipeProductMatcher.match(BigDecimal.valueOf(100), " G ", candidates);

        assertThat(result.status()).isEqualTo(ProductMatchStatus.MATCHED);
    }

    @Test
    void 필요_용량을_충족하는_가장_작은_상품을_고른다() {
        // 필요량 100g, 후보: 500g / 1000g / 2000g -> 가장 작은 500g을 골라야 함
        List<ProductCandidate> candidates = List.of(
                candidate(1L, "GRAM", 2000, null, 3000L),
                candidate(2L, "GRAM", 500, null, 2000L),
                candidate(3L, "GRAM", 1000, null, 1000L));

        Result result = RecipeProductMatcher.match(BigDecimal.valueOf(100), "g", candidates);

        assertThat(result.status()).isEqualTo(ProductMatchStatus.MATCHED);
        assertThat(result.capacitySufficient()).isTrue();
        assertThat(result.product().productId()).isEqualTo(2L);
    }

    @Test
    void 용량이_같으면_최저가_상품을_고른다() {
        List<ProductCandidate> candidates = List.of(
                candidate(1L, "GRAM", 500, null, 3000L),
                candidate(2L, "GRAM", 500, null, 1500L));

        Result result = RecipeProductMatcher.match(BigDecimal.valueOf(100), "g", candidates);

        assertThat(result.status()).isEqualTo(ProductMatchStatus.MATCHED);
        assertThat(result.product().productId()).isEqualTo(2L);
    }

    @Test
    void 필요_용량을_채우는_상품이_없으면_가장_큰_용량_상품을_capacitySufficient_false로_반환한다() {
        // 필요량 5000g, 후보: 500g / 1000g -> 둘 다 부족, 가장 큰 1000g을 capacitySufficient=false로
        List<ProductCandidate> candidates = List.of(
                candidate(1L, "GRAM", 500, null, 2000L),
                candidate(2L, "GRAM", 1000, null, 3000L));

        Result result = RecipeProductMatcher.match(BigDecimal.valueOf(5000), "g", candidates);

        assertThat(result.status()).isEqualTo(ProductMatchStatus.MATCHED);
        assertThat(result.capacitySufficient()).isFalse();
        assertThat(result.product().productId()).isEqualTo(2L);
    }

    @Test
    void KILOGRAM_단위는_1000을_곱해_그램으로_환산한다() {
        // 1kg = 1000g, 필요량 900g -> 충분
        List<ProductCandidate> candidates = List.of(candidate(1L, "KILOGRAM", 1, null, 1000L));

        Result result = RecipeProductMatcher.match(BigDecimal.valueOf(900), "g", candidates);

        assertThat(result.status()).isEqualTo(ProductMatchStatus.MATCHED);
        assertThat(result.capacitySufficient()).isTrue();
    }

    @Test
    void packageCount가_있으면_구성_개수를_곱해_전체_용량을_계산한다() {
        // 500g * 3개 = 1500g, 필요량 1200g -> 충분
        List<ProductCandidate> candidates = List.of(candidate(1L, "GRAM", 500, 3, 1000L));

        Result result = RecipeProductMatcher.match(BigDecimal.valueOf(1200), "g", candidates);

        assertThat(result.status()).isEqualTo(ProductMatchStatus.MATCHED);
        assertThat(result.capacitySufficient()).isTrue();
    }

    @Test
    void MILLILITER_LITER_단위_상품은_매칭_후보에서_제외되어_NO_PRODUCT를_반환한다() {
        // 식용유처럼 ml/L 단위인 상품만 있는 경우 (recipe는 g단위, 재료 매핑은 정상이어도 매칭은 안 됨)
        List<ProductCandidate> candidates = List.of(
                candidate(1L, "MILLILITER", 750, null, 5000L), candidate(2L, "LITER", 1, null, 7000L));

        Result result = RecipeProductMatcher.match(BigDecimal.valueOf(10), "g", candidates);

        assertThat(result.status()).isEqualTo(ProductMatchStatus.NO_PRODUCT);
        assertThat(result.product()).isNull();
    }

    @Test
    void candidates가_비어있으면_NO_PRODUCT를_반환한다() {
        Result result = RecipeProductMatcher.match(BigDecimal.valueOf(100), "g", List.of());

        assertThat(result.status()).isEqualTo(ProductMatchStatus.NO_PRODUCT);
    }

    @Test
    void capacity가_없는_후보는_매칭_대상에서_제외된다() {
        List<ProductCandidate> candidates = List.of(
                candidate(1L, "GRAM", null, null, 1000L), candidate(2L, "GRAM", 0, null, 1000L));

        Result result = RecipeProductMatcher.match(BigDecimal.valueOf(100), "g", candidates);

        assertThat(result.status()).isEqualTo(ProductMatchStatus.NO_PRODUCT);
    }

    private ProductCandidate candidate(Long productId, String unit, Integer capacity, Integer packageCount, Long price) {
        return new ProductCandidate(13L, productId, "테스트 상품", price, null, unit, capacity, packageCount);
    }
}

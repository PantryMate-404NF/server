package com.pantrymate.pantryrecipe.recipe.domain;

import com.pantrymate.pantryrecipe.recipe.domain.enums.ProductMatchStatus;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * 레시피 필요 용량(g)과 가장 근접한 판매 상품을 고른다.
 * 필요 용량을 채우는 상품 중 용량이 가장 작은 것, 없으면 용량이 가장 큰 것을 고르며 동률이면 최저가 순이다.
 * 레시피 단위가 g가 아니거나 필요량이 없는 재료는 매칭 대상이 아니다(UNSUPPORTED).
 */
public final class RecipeProductMatcher {

    private static final String GRAM_UNIT = "g";
    private static final BigDecimal GRAMS_PER_KILOGRAM = BigDecimal.valueOf(1000);

    private RecipeProductMatcher() {}

    public record Result(ProductMatchStatus status, ProductCandidate product, boolean capacitySufficient) {}

    private record Weighted(ProductCandidate product, BigDecimal grams) {}

    public static Result match(BigDecimal requiredAmount, String recipeUnit, List<ProductCandidate> candidates) {
        if (!isGramUnit(recipeUnit) || requiredAmount == null || requiredAmount.signum() <= 0) {
            return new Result(ProductMatchStatus.UNSUPPORTED, null, false);
        }

        Comparator<Weighted> tieBreak = Comparator.comparing((Weighted w) -> w.product().price())
                .thenComparing(w -> w.product().productId());

        List<Weighted> weighted = candidates.stream()
                .map(candidate -> new Weighted(candidate, gramsOf(candidate)))
                .filter(w -> w.grams() != null)
                .toList();

        return weighted.stream()
                .filter(w -> w.grams().compareTo(requiredAmount) >= 0)
                .min(Comparator.comparing(Weighted::grams).thenComparing(tieBreak))
                .map(w -> new Result(ProductMatchStatus.MATCHED, w.product(), true))
                .or(() -> weighted.stream()
                        .min(Comparator.comparing(Weighted::grams, Comparator.reverseOrder()).thenComparing(tieBreak))
                        .map(w -> new Result(ProductMatchStatus.MATCHED, w.product(), false)))
                .orElseGet(() -> new Result(ProductMatchStatus.NO_PRODUCT, null, false));
    }

    private static boolean isGramUnit(String recipeUnit) {
        return recipeUnit != null && GRAM_UNIT.equalsIgnoreCase(recipeUnit.trim());
    }

    private static BigDecimal gramsOf(ProductCandidate candidate) {
        if (candidate.capacity() == null || candidate.capacity() <= 0 || candidate.unit() == null) {
            return null;
        }
        // capacity는 구성 1개당 용량, packageCount는 구성 개수(미입력이면 1)로 보고 상품 전체 용량을 계산한다.
        int packageCount = candidate.packageCount() == null || candidate.packageCount() <= 0 ? 1 : candidate.packageCount();
        BigDecimal totalCapacity = BigDecimal.valueOf(candidate.capacity()).multiply(BigDecimal.valueOf(packageCount));
        return switch (candidate.unit()) {
            case "GRAM" -> totalCapacity;
            case "KILOGRAM" -> totalCapacity.multiply(GRAMS_PER_KILOGRAM);
            default -> null;
        };
    }
}

package com.pantrymate.pantryrecipe.recipe.domain;

import java.time.LocalDate;
import java.util.List;

public interface RecommendationPort {

    /** AI가 응답하지 못하면(타임아웃·5xx·연결 실패·계약 위반) RecommendationUnavailableException을 던진다. */
    Recommendation recommend(RecommendationQuery query);

    record RecommendationQuery(
            Long userId,
            String sessionId,
            int topK,
            Integer maxMinutes,
            List<PantryEntry> pantry,
            List<String> allergies) {}

    record PantryEntry(Long ingredientId, LocalDate purchasedAt, LocalDate expiresAt) {}

    record Recommendation(String requestId, String modelVersion, List<RecommendedItem> items) {}

    record RecommendedItem(
            Long recipeId, int finalRank, String reason, Double coverage, int missingCount, List<Long> missingIds) {}

    class RecommendationUnavailableException extends RuntimeException {
        public RecommendationUnavailableException(String message) {
            super(message);
        }
    }
}

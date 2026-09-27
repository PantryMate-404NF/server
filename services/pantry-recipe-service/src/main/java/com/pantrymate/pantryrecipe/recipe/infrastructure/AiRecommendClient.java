package com.pantrymate.pantryrecipe.recipe.infrastructure;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pantrymate.pantryrecipe.recipe.domain.RecommendationPort;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AiRecommendClient implements RecommendationPort {

    private static final Logger log = LoggerFactory.getLogger(AiRecommendClient.class);

    private final RestClient restClient;
    private final String apiKey;

    public AiRecommendClient(
            @Value("${ai.recommend.base-url}") String baseUrl,
            @Value("${ai.recommend.internal-api-key}") String apiKey,
            @Value("${ai.recommend.timeout-ms}") long timeoutMs) {
        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(HttpClient.newBuilder()
                        .connectTimeout(Duration.ofMillis(500))
                        .build());
        requestFactory.setReadTimeout(Duration.ofMillis(timeoutMs));
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
        this.apiKey = apiKey;
    }

    @Override
    public Recommendation recommend(RecommendationQuery query) {
        AiRequest request = new AiRequest(
                query.userId(),
                query.sessionId(),
                query.topK(),
                query.maxMinutes(),
                false,
                query.pantry().stream()
                        .map(entry -> new AiPantry(entry.ingredientId(), entry.purchasedAt(), entry.expiresAt()))
                        .toList(),
                query.allergies());
        try {
            AiResponse response = restClient
                    .post()
                    .uri("/v1/recommend")
                    .header("X-Internal-Api-Key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .exchange((req, res) -> {
                        int status = res.getStatusCode().value();
                        if (status == 200) {
                            return res.bodyTo(AiResponse.class);
                        }
                        if (status == 400 || status == 401) {
                            // 계약 위반(필드 누락·키 불일치)은 우리 쪽 버그이므로 눈에 띄게 남긴다.
                            log.error("AI 추천 요청이 거부됨 status={} userId={}", status, query.userId());
                        }
                        throw new RecommendationUnavailableException("AI 추천 응답 status=" + status);
                    });
            if (response == null || response.items() == null) {
                throw new RecommendationUnavailableException("AI 추천 응답 본문이 비어 있음");
            }
            List<RecommendedItem> items = response.items().stream()
                    .map(item -> new RecommendedItem(
                            item.recipeId(),
                            item.finalRank(),
                            item.reason(),
                            item.coverage(),
                            item.missingCount() == null ? 0 : item.missingCount(),
                            item.missingIds() == null ? List.of() : item.missingIds()))
                    .toList();
            return new Recommendation(response.requestId(), response.modelVersion(), items);
        } catch (RecommendationUnavailableException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RecommendationUnavailableException("AI 추천 호출 실패: " + e.getMessage());
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record AiRequest(
            @JsonProperty("user_id") Long userId,
            @JsonProperty("session_id") String sessionId,
            @JsonProperty("top_k") int topK,
            @JsonProperty("max_minutes") Integer maxMinutes,
            @JsonProperty("include_trace") boolean includeTrace,
            List<AiPantry> pantry,
            List<String> allergies) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record AiPantry(
            @JsonProperty("ingredient_id") Long ingredientId,
            @JsonProperty("purchased_at") LocalDate purchasedAt,
            @JsonProperty("expires_at") LocalDate expiresAt) {}

    private record AiResponse(
            @JsonProperty("request_id") String requestId,
            @JsonProperty("model_version") String modelVersion,
            List<AiItem> items) {}

    private record AiItem(
            @JsonProperty("recipe_id") Long recipeId,
            @JsonProperty("final_rank") int finalRank,
            String reason,
            Double coverage,
            @JsonProperty("missing_count") Integer missingCount,
            @JsonProperty("missing_ids") List<Long> missingIds) {}
}

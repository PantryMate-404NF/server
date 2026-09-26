package com.pantrymate.pantryrecipe.ai.infrastructure;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pantrymate.pantryrecipe.ai.domain.AiEvent;
import com.pantrymate.pantryrecipe.ai.domain.AiEventSink;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AiEventClient implements AiEventSink {

    private static final Logger log = LoggerFactory.getLogger(AiEventClient.class);

    private final RestClient restClient;
    private final String apiKey;

    public AiEventClient(
            @Value("${ai.events.base-url}") String baseUrl,
            @Value("${ai.events.internal-api-key}") String apiKey,
            @Value("${ai.events.timeout-ms}") long timeoutMs) {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofMillis(500)).build());
        requestFactory.setReadTimeout(Duration.ofMillis(timeoutMs));
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
        this.apiKey = apiKey;
    }

    @Override
    public Result send(List<AiEvent> events) {
        AiRequest request = new AiRequest(events.stream()
                .map(e -> new AiPayload(
                        e.getUserId(), e.getEventType(), e.getRecipeId(), e.getRequestId(), e.getPosition(), e.getOccurredAt()))
                .toList());
        try {
            return restClient
                    .post()
                    .uri("/v1/events")
                    .header("X-Internal-Api-Key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .exchange((req, res) -> {
                        int status = res.getStatusCode().value();
                        if (status == 200) {
                            return Result.OK;
                        }
                        if (status == 401 || status == 403 || status == 408 || status == 429 || status >= 500) {
                            // 키 불일치 등 설정 문제는 이벤트를 버리지 않고 남겨 둔다.
                            log.warn("AI 이벤트 전송 실패 status={} count={}", status, events.size());
                            return Result.RETRYABLE;
                        }
                        log.warn("AI 이벤트 거부됨 status={} count={}", status, events.size());
                        return Result.REJECTED;
                    });
        } catch (RuntimeException e) {
            log.warn("AI 이벤트 호출 실패: {}", e.getMessage());
            return Result.RETRYABLE;
        }
    }

    private record AiRequest(List<AiPayload> events) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record AiPayload(
            @JsonProperty("user_id") Long userId,
            @JsonProperty("event_type") String eventType,
            @JsonProperty("recipe_id") Long recipeId,
            @JsonProperty("request_id") String requestId,
            Integer position,
            @JsonProperty("occurred_at") OffsetDateTime occurredAt) {}
}

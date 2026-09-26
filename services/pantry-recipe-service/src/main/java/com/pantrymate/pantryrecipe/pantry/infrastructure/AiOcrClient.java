package com.pantrymate.pantryrecipe.pantry.infrastructure;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pantrymate.pantryrecipe.pantry.domain.ReceiptOcrGateway;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class AiOcrClient implements ReceiptOcrGateway {

    private static final Logger log = LoggerFactory.getLogger(AiOcrClient.class);
    private static final String LLM_UNAVAILABLE = "LLM_UNAVAILABLE";
    private static final String UNAVAILABLE_MESSAGE = "일시적인 오류로 영수증을 분석하지 못했습니다. 잠시 후 다시 시도하거나 직접 등록해 주세요.";

    private final RestClient restClient;
    private final String apiKey;

    public AiOcrClient(
            @Value("${ai.ocr.base-url}") String baseUrl,
            @Value("${ai.ocr.internal-api-key}") String apiKey,
            @Value("${ai.ocr.timeout-seconds}") long timeoutSeconds) {
        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(3))
                        .build());
        // 명세: BE 호출 타임아웃 30초, 재시도 없음
        requestFactory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
        this.apiKey = apiKey;
    }

    @Override
    public OcrResult recognize(
            String receiptId, String requestId, String filename, String contentType, byte[] image) {
        // 비ASCII 파일명이 멀티파트 헤더에서 깨지지 않도록 확장자만 살린 고정 이름을 쓴다.
        MediaType imageType = MediaType.parseMediaType(contentType);
        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentType(imageType);
        fileHeaders.setContentDisposition(
                ContentDisposition.formData().name("file").filename(safeFilename(imageType)).build());
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("receipt_id", receiptId);
        body.add("file", new HttpEntity<>(new ByteArrayResource(image), fileHeaders));
        try {
            return restClient
                    .post()
                    .uri("/v1/ocr/receipts")
                    .header("X-Internal-Api-Key", apiKey)
                    .header("X-Request-Id", requestId)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .exchange((request, response) -> {
                        int status = response.getStatusCode().value();
                        if (status == 200 || status == 500) {
                            AiOcrPayload payload = response.bodyTo(AiOcrPayload.class);
                            if (payload != null) {
                                return toResult(receiptId, payload);
                            }
                        }
                        log.error("AI OCR 호출 실패 status={} requestId={}", status, requestId);
                        return unavailable(receiptId);
                    });
        } catch (RuntimeException e) {
            log.error("AI OCR 호출 중 오류 requestId={}: {}", requestId, e.getMessage());
            return unavailable(receiptId);
        }
    }

    private String safeFilename(MediaType imageType) {
        return "receipt." + switch (imageType.getSubtype().toLowerCase()) {
            case "jpeg" -> "jpg";
            case "heif" -> "heic";
            default -> imageType.getSubtype().toLowerCase();
        };
    }

    private OcrResult toResult(String receiptId, AiOcrPayload payload) {
        List<OcrItem> items = payload.items() == null
                ? List.of()
                : payload.items().stream()
                        .map(item -> new OcrItem(item.name(), item.ingredientId()))
                        .toList();
        OcrError error = payload.error() == null
                ? null
                : new OcrError(payload.error().code(), payload.error().message());
        return new OcrResult(
                payload.receiptId() == null ? receiptId : payload.receiptId(), payload.purchasedAt(), items, error);
    }

    private OcrResult unavailable(String receiptId) {
        return new OcrResult(receiptId, null, List.of(), new OcrError(LLM_UNAVAILABLE, UNAVAILABLE_MESSAGE));
    }

    private record AiOcrPayload(
            @JsonProperty("receipt_id") String receiptId,
            @JsonProperty("purchased_at") String purchasedAt,
            List<AiItem> items,
            AiError error) {}

    private record AiItem(String name, @JsonProperty("ingredient_id") Long ingredientId) {}

    private record AiError(String code, String message) {}
}

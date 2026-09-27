package com.pantrymate.pantryrecipe.pantry.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pantrymate.pantryrecipe.pantry.domain.ReceiptOcrGateway.OcrResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "AI OCR 응답 구조를 그대로 따른다. 성공은 HTTP 200, 실패(OCR_EMPTY·LLM_UNAVAILABLE)는 HTTP 500이며 본문 구조는 같다.")
public record ReceiptOcrResponseDto(
        @JsonProperty("receipt_id") @Schema(description = "요청한 영수증 ID", example = "01K4A7Q3ZV8XG2M5W9R1DTF6HJ")
                String receiptId,
        @JsonProperty("purchased_at") @Schema(description = "구매일(YYYY-MM-DD). 못 읽으면 null", example = "2026-01-30", nullable = true)
                String purchasedAt,
        @Schema(description = "식재료로 판정된 구매 품목. 0개일 수 있음") List<Item> items,
        @JsonInclude(JsonInclude.Include.NON_NULL) @Schema(description = "실패 시에만 존재") Error error) {

    @Schema(name = "ReceiptOcrItem")
    public record Item(
            @Schema(description = "영수증 표기 기준 품목명(최대 20자)", example = "깐마늘") String name,
            @JsonProperty("ingredient_id") @Schema(description = "식재료 사전 ID. 미매칭이면 null", nullable = true) Long ingredientId) {}

    @Schema(name = "ReceiptOcrError")
    public record Error(
            @Schema(description = "OCR_EMPTY / LLM_UNAVAILABLE", example = "OCR_EMPTY") String code,
            @Schema(description = "사용자에게 그대로 보여줄 수 있는 한국어 메시지") String message) {}

    public static ReceiptOcrResponseDto of(OcrResult result, List<Item> items) {
        Error error = result.error() == null ? null : new Error(result.error().code(), result.error().message());
        return new ReceiptOcrResponseDto(result.receiptId(), result.purchasedAt(), items, error);
    }
}

package com.pantrymate.pantryrecipe.pantry.presentation.controller;

import com.pantrymate.common.dto.CurrentUser;
import com.pantrymate.pantryrecipe.pantry.application.ReceiptOcrService;
import com.pantrymate.pantryrecipe.pantry.presentation.dto.ReceiptOcrResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "PANTRY", description = "팬트리 식재료 관리")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/pantry-items/receipts")
public class PantryReceiptController {

    private final ReceiptOcrService receiptOcrService;

    public PantryReceiptController(ReceiptOcrService receiptOcrService) {
        this.receiptOcrService = receiptOcrService;
    }

    @Operation(
            summary = "영수증 사진 OCR 분석",
            description = "영수증 이미지 1장(JPEG·PNG·WebP·HEIC, 10MB 이하)과 프론트가 발급한 receiptId로 구매일·품목명 후보를 반환한다. "
                    + "응답까지 보통 5~10초, 최대 30초이므로 로딩 상태를 표시해야 한다. "
                    + "성공은 200, 인식 실패(OCR_EMPTY)·AI 장애(LLM_UNAVAILABLE)는 500이며 본문 구조는 같다(공통 응답 래퍼 없음). "
                    + "결과는 등록 후보이므로 사용자가 확인·수정한 뒤 POST /api/pantry-items를 항목마다 호출해 등록하고, "
                    + "이때 purchaseDate에 purchased_at을 넣는다. purchased_at이 null이면 사용자가 직접 입력한다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "분석 성공(품목 0개일 수 있음)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400", description = "PANTRY-INVALID-RECEIPT (공통 응답 규격)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "500", description = "error.code = OCR_EMPTY / LLM_UNAVAILABLE")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReceiptOcrResponseDto> recognize(
            @Parameter(hidden = true) CurrentUser currentUser,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @RequestParam(value = "receiptId", required = false) String receiptId,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        ReceiptOcrResponseDto response = receiptOcrService.recognize(receiptId, requestId, file);
        HttpStatus status = response.error() == null ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(response);
    }
}

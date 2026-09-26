package com.pantrymate.pantryrecipe.pantry.domain;

import java.util.List;

public interface ReceiptOcrGateway {

    /** 영수증 이미지 1장을 분석한다. 분석 실패는 예외가 아니라 error가 채워진 결과로 돌려준다. */
    OcrResult recognize(String receiptId, String requestId, String filename, String contentType, byte[] image);

    record OcrResult(String receiptId, String purchasedAt, List<OcrItem> items, OcrError error) {}

    record OcrItem(String name, Long ingredientId) {}

    record OcrError(String code, String message) {}
}

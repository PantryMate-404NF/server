package com.pantrymate.product.product.domain.exception;

import com.pantrymate.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {

    DUPLICATE_SKU(HttpStatus.BAD_REQUEST, "PRODUCT-001", "이미 사용 중인 상품 코드입니다."),
    CATEGORY_NOT_FOUND(HttpStatus.BAD_REQUEST, "PRODUCT-002", "존재하지 않는 카테고리입니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT-003", "상품을 찾을 수 없습니다."),
    THUMBNAIL_REQUIRED(HttpStatus.BAD_REQUEST, "PRODUCT-004", "대표 이미지를 등록해 주세요."),
    INVALID_PRICE(HttpStatus.BAD_REQUEST, "PRODUCT-005", "올바른 가격을 입력해 주세요."),
    INVALID_PRODUCT_STATUS(HttpStatus.CONFLICT, "PRODUCT-006", "판매중단 상태의 상품만 삭제할 수 있습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
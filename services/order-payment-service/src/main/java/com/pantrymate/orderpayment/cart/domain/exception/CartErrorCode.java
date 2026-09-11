package com.pantrymate.orderpayment.cart.domain.exception;

import com.pantrymate.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CartErrorCode implements ErrorCode {

    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "CART-001", "존재하지 않는 상품입니다."),
    PRODUCT_UNAVAILABLE(HttpStatus.CONFLICT, "CART-002", "구매할 수 없는 상품입니다."),
    STOCK_EXCEEDED(HttpStatus.CONFLICT, "CART-003", "재고가 부족합니다."),
    INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "CART-004", "수량은 1개 이상이어야 합니다."),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "CART-005", "장바구니 항목을 찾을 수 없습니다."),
    PRODUCT_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "CART-006", "잠시 후 다시 시도해 주세요.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
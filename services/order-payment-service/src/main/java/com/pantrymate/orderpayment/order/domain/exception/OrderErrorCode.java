package com.pantrymate.orderpayment.order.domain.exception;

import com.pantrymate.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {

    EMPTY_SELECTION(HttpStatus.BAD_REQUEST, "ORDER-001", "주문할 상품을 선택해 주세요."),
    PRODUCT_OUT_OF_STOCK(HttpStatus.CONFLICT, "ORDER-002", "선택한 상품 중 품절된 상품이 있습니다."),
    PRODUCT_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "ORDER-003", "잠시 후 다시 시도해 주세요."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER-004", "주문을 찾을 수 없습니다."),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER-005", "장바구니 항목을 찾을 수 없습니다."),
    INVALID_IDEMPOTENCY_KEY(HttpStatus.CONFLICT, "ORDER-006", "키 값이 일치하지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

}

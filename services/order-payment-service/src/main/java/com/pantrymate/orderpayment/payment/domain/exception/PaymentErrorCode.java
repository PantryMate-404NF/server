package com.pantrymate.orderpayment.payment.domain.exception;

import com.pantrymate.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {

    NOT_SUPPORT_STATUS(HttpStatus.CONFLICT, "PAYMENT-001", "지원하지 않는 상태입니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT-002", "결제 정보를 찾을 수 없습니다."),
    ORDER_ALREADY_PAID(HttpStatus.CONFLICT, "PAYMENT-003", "이미 결제가 완료된 주문입니다."),
    AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "PAYMENT-004", "결제 금액이 일치하지 않습니다."),
    TOSS_API_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "PAYMENT-005", "결제 처리 중 오류가 발생했습니다."),
    STOCK_DEDUCTION_FAILED(HttpStatus.CONFLICT, "PAYMENT-006", "재고 처리 중 문제가 발생했습니다."),
    PAYMENT_IN_PROGRESS(HttpStatus.CONFLICT, "PAYMENT-007", "이미 처리 중인 결제 요청입니다."),
    CANCEL_FAILED(HttpStatus.CONFLICT, "PAYMENT-008", "결제 취소에 실패했습니다."),
    ALREADY_CANCELED(HttpStatus.CONFLICT, "PAYMENT-009", "이미 취소된 결제입니다."),
    PAYMENT_FAILED(HttpStatus.CONFLICT, "PAYMENT-010", "결제에 실패하였습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;


    }

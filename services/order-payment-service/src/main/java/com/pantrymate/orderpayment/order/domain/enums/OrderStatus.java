package com.pantrymate.orderpayment.order.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatus {
    PENDING("결제대기", "주문서가 생성되었습니다."),
    CONFIRMED("결제완료", "결제가 성공적으로 완료되었습니다."),
    UNKNOWN_HOLD("확인중", "결제 결과를 확인하고 있습니다."),
    FAILED("주문실패", "주문이 실패되었습니다."),
    CANCEL_REQUESTED("취소요청", "주문 취소 요청을 처리중입니다."),
    CANCELLED("취소완료", "주문이 취소되었습니다.");

    private final String label;
    private final String description;

    public boolean canTransitionTo(OrderStatus target) {
        return switch (this) {
            case PENDING -> target == CONFIRMED || target == UNKNOWN_HOLD || target == CANCEL_REQUESTED;
            case CONFIRMED -> target == CANCEL_REQUESTED;
            case UNKNOWN_HOLD -> target == CONFIRMED || target == CANCELLED;
            case CANCEL_REQUESTED -> target == CANCELLED;
            case FAILED, CANCELLED -> false;
        };
    }
}

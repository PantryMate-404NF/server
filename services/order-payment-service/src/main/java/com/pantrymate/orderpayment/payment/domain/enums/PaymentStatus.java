package com.pantrymate.orderpayment.payment.domain.enums;

import com.pantrymate.common.exception.BusinessException;
import com.pantrymate.orderpayment.payment.domain.exception.PaymentErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    READY("결제대기", "결제 생성 후 인증 전 상태입니다."),
    IN_PROGRESS("인증완료", "결제수단 인증이 완료되어 승인 대기 중입니다."),
    DONE("결제완료", "결제 승인이 완료되었습니다."),
    CANCELED("결제취소", "결제가 취소되었습니다."),
    ABORTED("결제실패", "결제 승인에 실패했습니다."),
    EXPIRED("시간만료", "결제 유효 시간이 만료되었습니다."),
    UNKNOWN_HOLD("확인중", "결제 결과를 확인하고 있습니다.");

    private final String label;
    private final String description;

    public static PaymentStatus from(String status) {
        try {
            return PaymentStatus.valueOf(status);
        }catch (IllegalArgumentException e) {
            throw new BusinessException(PaymentErrorCode.NOT_SUPPORT_STATUS);
        }
    }

}

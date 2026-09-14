package com.pantrymate.orderpayment.payment.application.dto;

import com.pantrymate.orderpayment.payment.domain.Payments;
import java.time.LocalDateTime;

public record PaymentDetailResponse(
    String paymentKey,
    Long orderId,
    Long totalAmount,
    String method,
    String status,
    LocalDateTime createdAt,
    LocalDateTime approveAt
) {
    public static PaymentDetailResponse from(Payments payment) {
        return new PaymentDetailResponse(
            payment.getPaymentKey(),
            payment.getOrderId(),
            payment.getTotalAmount(),
            payment.getMethod(),
            payment.getStatus().name(),
            payment.getCreatedAt(),
            payment.getApprovedAt()
        );
    }

}

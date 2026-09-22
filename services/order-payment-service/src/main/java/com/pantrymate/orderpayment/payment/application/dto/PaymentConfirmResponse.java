package com.pantrymate.orderpayment.payment.application.dto;

import com.pantrymate.orderpayment.payment.domain.Payments;

public record PaymentConfirmResponse(
    String paymentKey,
    Long totalAmount,
    String status,
    String approveAt

) {
    public static PaymentConfirmResponse from(Payments payments) {
        return new PaymentConfirmResponse(
            payments.getPaymentKey(),
            payments.getTotalAmount(),
            payments.getStatus().name(),
            payments.getApprovedAt() != null ? payments.getApprovedAt().toString() : null
        );
    }

}

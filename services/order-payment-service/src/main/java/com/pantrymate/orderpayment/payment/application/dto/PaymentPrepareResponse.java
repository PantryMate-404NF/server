package com.pantrymate.orderpayment.payment.application.dto;

import com.pantrymate.orderpayment.payment.domain.Payments;

public record PaymentPrepareResponse(
    Long paymentId,
    Long orderId,
    Long totalAmount,
    String status
) {

    public static PaymentPrepareResponse from(Payments payments) {
        return new PaymentPrepareResponse(
            payments.getId(),
            payments.getOrderId(),
            payments.getTotalAmount(),
            payments.getStatus().name()
        );
    }
}

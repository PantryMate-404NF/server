package com.pantrymate.orderpayment.payment.application.dto;

import com.pantrymate.orderpayment.order.domain.Orders;
import com.pantrymate.orderpayment.payment.domain.Payments;

public record PaymentPrepareResponse(
    Long paymentId,
    String orderId,
    Long totalAmount,
    String status
) {

    public static PaymentPrepareResponse of(Payments payments, Orders order) {
        return new PaymentPrepareResponse(
            payments.getId(),
            order.getOrderId(),
            payments.getTotalAmount(),
            payments.getStatus().name()
        );
    }
}

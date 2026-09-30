package com.pantrymate.orderpayment.payment.application.dto;

import com.pantrymate.orderpayment.order.domain.Orders;
import com.pantrymate.orderpayment.payment.domain.Payments;
import java.time.LocalDateTime;

public record PaymentDetailResponse(
    String paymentKey,
    String orderId,
    Long totalAmount,
    Long productAmount,
    Long shippingFee,
    String method,
    String status,
    LocalDateTime createdAt,
    LocalDateTime approveAt
) {

    public static PaymentDetailResponse of(Payments payment, Orders order) {
        return new PaymentDetailResponse(
            payment.getPaymentKey(),
            order.getOrderId(),
            payment.getTotalAmount(),
            payment.getTotalAmount() - Orders.SHIPPING_FEE,
            Orders.SHIPPING_FEE,
            payment.getMethod(),
            payment.getStatus().name(),
            payment.getCreatedAt(),
            payment.getApprovedAt()
        );
    }

}

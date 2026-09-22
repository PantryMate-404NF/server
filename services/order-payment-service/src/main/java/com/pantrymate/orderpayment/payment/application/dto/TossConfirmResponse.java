package com.pantrymate.orderpayment.payment.application.dto;

public record TossConfirmResponse(
    String paymentKey,
    String orderId,
    String status,
    String method,
    Long totalAmount
) {

}

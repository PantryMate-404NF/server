package com.pantrymate.orderpayment.payment.application.dto;

public record TossConfirmRequest(
    String paymentKey,
    String orderId,
    Long amount
) {

}

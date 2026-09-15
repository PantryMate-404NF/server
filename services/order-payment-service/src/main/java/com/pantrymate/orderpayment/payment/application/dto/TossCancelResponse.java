package com.pantrymate.orderpayment.payment.application.dto;

public record TossCancelResponse(
    String paymentKey,
    String status,
    Long totalAmount
) {

}

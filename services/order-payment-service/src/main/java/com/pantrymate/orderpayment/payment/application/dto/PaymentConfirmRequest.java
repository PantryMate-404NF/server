package com.pantrymate.orderpayment.payment.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentConfirmRequest(
    @NotBlank
    String paymentKey,
    @NotBlank
    String orderId,
    @NotNull
    Long amount
) {

}

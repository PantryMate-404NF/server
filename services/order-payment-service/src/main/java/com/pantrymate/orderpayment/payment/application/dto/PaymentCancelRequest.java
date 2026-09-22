package com.pantrymate.orderpayment.payment.application.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentCancelRequest(
    @NotBlank
    String cancelReason
) {

}

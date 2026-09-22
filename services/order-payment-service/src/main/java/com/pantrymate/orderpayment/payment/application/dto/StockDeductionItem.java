package com.pantrymate.orderpayment.payment.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StockDeductionItem(
    @NotNull
    Long productId,

    @NotNull
    @Positive
    Integer quantity
) {

}

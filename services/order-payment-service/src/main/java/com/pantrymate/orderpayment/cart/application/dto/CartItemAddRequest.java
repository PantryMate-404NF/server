package com.pantrymate.orderpayment.cart.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CartItemAddRequest(
    @NotNull
    Long productId,

    @NotNull
    @Positive
    Integer quantity
) {

}

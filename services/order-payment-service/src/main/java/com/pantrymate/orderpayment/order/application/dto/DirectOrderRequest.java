package com.pantrymate.orderpayment.order.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DirectOrderRequest(
    @NotNull
    Long productId,

    @NotNull
    @Positive
    Integer quantity,

    @NotNull
    @Valid
    DeliveryAddressRequest deliveryAddress
) {

}

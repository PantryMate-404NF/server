package com.pantrymate.orderpayment.cart.application.dto;

import jakarta.persistence.criteria.CriteriaBuilder.In;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CartItemUpdateRequest(
    @NotNull
    @Positive
    Integer quantity
) {

}

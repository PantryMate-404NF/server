package com.pantrymate.product.product.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProductRestockRequest(
    @NotNull
    @Positive
    Integer quantity
) {

}

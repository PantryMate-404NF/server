package com.pantrymate.orderpayment.order.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record OrderCreateRequest(
    @NotNull
    Long cartId,

    @NotEmpty
    List<Long> selectedCartItemIds
) {

}

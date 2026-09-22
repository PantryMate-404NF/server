package com.pantrymate.orderpayment.payment.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record StockDeductionRequest(
    @NotEmpty
    @Valid
    List<StockDeductionItem> items
) {

}

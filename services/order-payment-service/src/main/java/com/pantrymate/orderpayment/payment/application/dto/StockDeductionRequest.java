package com.pantrymate.orderpayment.payment.application.dto;

import java.util.List;

public record StockDeductionRequest(
    List<StockDeductionItem> items
) {

}

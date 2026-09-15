package com.pantrymate.orderpayment.payment.application.dto;

public record StockDeductionItem(
    Long productId,
    Integer quantity
) {

}

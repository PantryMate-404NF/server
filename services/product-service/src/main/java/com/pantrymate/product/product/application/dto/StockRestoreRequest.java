package com.pantrymate.product.product.application.dto;

import java.util.List;

public record StockRestoreRequest(
    List<StockDeductionItem> items
){

}

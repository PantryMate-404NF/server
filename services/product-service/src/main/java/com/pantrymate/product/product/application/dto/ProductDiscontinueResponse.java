package com.pantrymate.product.product.application.dto;

import com.pantrymate.product.product.domain.Products;

public record ProductDiscontinueResponse(
    Long productId,
    String status
) {
    public static ProductDiscontinueResponse from(Products product){
        return new ProductDiscontinueResponse(
            product.getProductId(),
            product.getStatus().name()
        );
    }
}

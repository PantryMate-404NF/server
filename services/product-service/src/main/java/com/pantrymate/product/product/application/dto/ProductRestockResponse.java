package com.pantrymate.product.product.application.dto;

import com.pantrymate.product.product.domain.Products;

public record ProductRestockResponse(
    Long productId,
    Integer stockQuantity,
    String status

) {
    public static ProductRestockResponse from(Products product){
        return new ProductRestockResponse(
            product.getProductId(),
            product.getStockQuantity(),
            product.getStatus().name()
        );
    }
}

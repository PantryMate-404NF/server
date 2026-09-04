package com.pantrymate.product.product.application.dto;

import com.pantrymate.product.product.domain.Products;

public record ProductSummaryResponse(
    Long productId,
    String name,
    Long price,
    String thumbnailUrl,
    String status
) {

    public static ProductSummaryResponse from(Products product) {
        return new  ProductSummaryResponse(
            product.getProductId(),
            product.getName(),
            product.getPrice(),
            product.getThumbnailUrl(),
            product.getStatus().name()
        );
    }
}

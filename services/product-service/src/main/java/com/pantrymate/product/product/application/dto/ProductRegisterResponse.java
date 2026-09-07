package com.pantrymate.product.product.application.dto;

import com.pantrymate.product.product.domain.Products;
import java.time.LocalDateTime;

public record ProductRegisterResponse(
    Long productId,
    String sku,
    String name,
    String status,
    LocalDateTime createdAt
) {

    public static ProductRegisterResponse from(Products product) {
        return new ProductRegisterResponse(
            product.getProductId(),
            product.getSku(),
            product.getName(),
            product.getStatus().name(),
            product.getCreatedAt()
        );
    }
}

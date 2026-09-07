package com.pantrymate.product.product.application.dto;

import com.pantrymate.product.product.domain.Products;
import java.time.LocalDateTime;

public record ProductDeleteResponse(
    Long productId,
    LocalDateTime deletedAt
) {
    public static ProductDeleteResponse from(Products product){
        return new ProductDeleteResponse(
            product.getProductId(),
            product.getDeletedAt()
        );
    }
}

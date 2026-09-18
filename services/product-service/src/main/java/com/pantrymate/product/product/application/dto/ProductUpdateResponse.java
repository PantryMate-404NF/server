package com.pantrymate.product.product.application.dto;

import com.pantrymate.product.product.domain.Products;
import java.time.LocalDateTime;

public record ProductUpdateResponse(
    Long productId,
    LocalDateTime updateAt
) {

    public static ProductUpdateResponse from(Products product) {
        return new ProductUpdateResponse(product.getProductId(),
            product.getUpdatedAt()
        );
    }

}

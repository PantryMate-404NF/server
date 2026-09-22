package com.pantrymate.product.product.application.dto;

import com.pantrymate.product.product.domain.ProductImages;
import com.pantrymate.product.product.domain.Products;
import jakarta.persistence.criteria.CriteriaBuilder.In;

public record ProductImageResponse(
    String imageUrl,
    String description,
    Integer sortOrder
) {

    public static ProductImageResponse from(ProductImages image)
    {
        return new ProductImageResponse(
            image.getImageUrl(),
            image.getDescription(),
            image.getSortOrder()
        );
    }
}

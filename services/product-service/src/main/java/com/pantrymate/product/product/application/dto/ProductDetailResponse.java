package com.pantrymate.product.product.application.dto;

import com.pantrymate.product.product.domain.Products;
import java.util.List;

public record ProductDetailResponse(
    Long productId,
    String sku,
    String name,
    Long categoryId,
    String categoryName,
    Long price,
    String unit,
    Integer capacity,
    Integer packageCount,
    String origin,
    String description,
    String thumbnailUrl,
    List<ProductImageResponse> images,
    Integer stockQuantity,
    String status
) {

    public static ProductDetailResponse of(Products products, String categoryName,
        List<ProductImageResponse> images) {
        return new ProductDetailResponse(
            products.getProductId(),
            products.getSku(),
            products.getName(),
            products.getCategoryId(),
            categoryName,
            products.getPrice(),
            products.getUnit() == null ? null : products.getUnit().name(),
            products.getCapacity(),
            products.getPackageCount(),
            products.getOrigin(),
            products.getDescription(),
            products.getThumbnailUrl(),
            images,
            products.getStockQuantity(),
            products.getStatus().name()
        );
    }
}

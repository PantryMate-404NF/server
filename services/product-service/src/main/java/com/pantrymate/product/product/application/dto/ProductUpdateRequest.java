package com.pantrymate.product.product.application.dto;

public record ProductUpdateRequest(
    String name,
    Long categoryId,
    Long price,
    String unit,
    Integer capacity,
    Integer packageCount,
    String origin,
    String description,
    String thumbnailUrl,
    Long ingredientId
) {

}

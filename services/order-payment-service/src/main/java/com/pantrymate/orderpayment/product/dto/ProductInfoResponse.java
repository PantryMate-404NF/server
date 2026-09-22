package com.pantrymate.orderpayment.product.dto;

public record ProductInfoResponse(
    Long productId,
    String name,
    Long price,
    String thumbnailUrl,
    Integer stockQuantity,
    String status
) {

}

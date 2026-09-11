package com.pantrymate.orderpayment.cart.application.dto;

public record ProductInfoResponse(
    Long productId,
    String name,
    Long price,
    String thumbnailUrl,
    Integer stockQuantity,
    String status
) {

}

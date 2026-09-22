package com.pantrymate.orderpayment.cart.application.dto;

import com.pantrymate.orderpayment.cart.domain.CartItems;
import com.pantrymate.orderpayment.product.dto.ProductInfoResponse;

public record CartItemResponse(
    Long cartItemId,
    Long productId,
    String productName,
    String thumbnailUrl,
    Long price,
    Integer quantity,
    String status,
    Boolean purchasable
) {
    public static CartItemResponse of(CartItems items, ProductInfoResponse product) {
        boolean purchasable = "ON_SALE".equals(product.status())
            && product.stockQuantity() >= items.getQuantity();

        return new CartItemResponse(
            items.getId(),
            items.getProductId(),
            product.name(),
            product.thumbnailUrl(),
            product.price(),
            items.getQuantity(),
            product.status(),
            purchasable
        );
    }
    public static CartItemResponse unavailable(CartItems item){
        return new CartItemResponse(
            item.getId(),
            item.getProductId(),
            "판매가 종료된 상품입니다.",
            null,
            0L,
            item.getQuantity(),
            "DISCONTINUED",
            false
        );
    }
}
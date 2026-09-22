package com.pantrymate.orderpayment.cart.application.dto;

import com.pantrymate.orderpayment.cart.domain.CartItems;

public record CartItemAddResponse(
    Long cartItemId,
    Long productId,
    Integer quantity

) {

    public static CartItemAddResponse from(CartItems cartItems) {
        return new CartItemAddResponse(
            cartItems.getId(),
            cartItems.getProductId(),
            cartItems.getQuantity()
        );
    }

}

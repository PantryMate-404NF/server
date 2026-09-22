package com.pantrymate.orderpayment.cart.application.dto;

import java.util.List;

public record CartResponse(
    Long cartId,
    List<CartItemResponse> items

) {

    public static CartResponse of(Long cartId, List<CartItemResponse> items) {
        return new CartResponse(cartId, items);
    }

}

package com.pantrymate.orderpayment.order.application.dto;

import com.pantrymate.orderpayment.order.domain.OrderItems;

public record OrderItemDetail(
    Long orderItemId,
    Long productId,
    String productName,
    Long price,
    Integer quantity
) {
    public static OrderItemDetail from(OrderItems orderItem) {
        return new OrderItemDetail(
            orderItem.getId(),
            orderItem.getProductId(),
            orderItem.getProductName(),
            orderItem.getPrice(),
            orderItem.getQuantity()
        );
    }
}

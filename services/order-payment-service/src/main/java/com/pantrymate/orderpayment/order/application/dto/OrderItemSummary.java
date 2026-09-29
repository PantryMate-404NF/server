package com.pantrymate.orderpayment.order.application.dto;

import com.pantrymate.orderpayment.order.domain.OrderItems;

public record OrderItemSummary(
    Long orderItemId,
    Long productId
) {
    public static OrderItemSummary from(OrderItems orderItem) {
        return new OrderItemSummary(orderItem.getId(), orderItem.getProductId());
    }
}
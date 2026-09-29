package com.pantrymate.orderpayment.order.application.dto;

import com.pantrymate.orderpayment.order.domain.Orders;
import java.time.LocalDateTime;
import java.util.List;

public record OrderSummaryResponse(
    String orderId,
    String orderName,
    Long totalAmount,
    String status,
    LocalDateTime createdAt,
    List<OrderItemSummary> items
) {

    public static OrderSummaryResponse of(Orders order, List<OrderItemSummary> items) {
        return new OrderSummaryResponse(
            order.getOrderId(),
            order.getOrderName(),
            order.getTotalAmount(),
            order.getStatus().name(),
            order.getCreatedAt(),
            items
        );
    }

}

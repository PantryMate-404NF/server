package com.pantrymate.orderpayment.order.application.dto;

import com.pantrymate.orderpayment.order.domain.Orders;
import java.time.LocalDateTime;

public record OrderSummaryResponse(
    String orderId,
    String orderName,
    Long totalAmount,
    String status,
    LocalDateTime createdAt
) {
    public static OrderSummaryResponse from(Orders order) {
        return new OrderSummaryResponse(
            order.getOrderId(),
            order.getOrderName(),
            order.getTotalAmount(),
            order.getStatus().name(),
            order.getCreatedAt()
        );
    }

}

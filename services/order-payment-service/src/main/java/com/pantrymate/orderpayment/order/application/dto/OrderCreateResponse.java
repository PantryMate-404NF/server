package com.pantrymate.orderpayment.order.application.dto;

import com.pantrymate.orderpayment.order.domain.Orders;
import java.time.LocalDateTime;

public record OrderCreateResponse(
    String orderId,
    Long totalAmount,
    String name,
    String status,
    LocalDateTime createdAt

) {
    public static OrderCreateResponse from(Orders order) {
        return new OrderCreateResponse(
            order.getOrderId(),
            order.getTotalAmount(),
            order.getOrderName(),
            order.getStatus().name(),
            order.getCreatedAt()
        );
    }

}

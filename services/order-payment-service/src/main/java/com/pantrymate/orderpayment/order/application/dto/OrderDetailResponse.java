package com.pantrymate.orderpayment.order.application.dto;

import com.pantrymate.orderpayment.order.domain.Orders;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponse(
    String orderId,
    String orderName,
    Long totalAmount,
    Long productAmount,
    Long shippingFee,
    String status,
    String recipientName,
    String recipientPhone,
    String zipCode,
    String address,
    String addressDetail,
    LocalDateTime createdAt,
    List<OrderItemDetail> items
) {
    public static OrderDetailResponse of(Orders order, List<OrderItemDetail> items) {
        return new OrderDetailResponse(
            order.getOrderId(),
            order.getOrderName(),
            order.getTotalAmount(),
            order.getTotalAmount() - Orders.SHIPPING_FEE,
            Orders.SHIPPING_FEE,
            order.getStatus().name(),
            order.getRecipientName(),
            order.getRecipientPhone(),
            order.getZipCode(),
            order.getAddress(),
            order.getAddressDetail(),
            order.getCreatedAt(),
            items
        );
    }
}

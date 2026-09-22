package com.pantrymate.orderpayment.order.application.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public record OrderListResponse(
    List<OrderSummaryResponse> content,
    long totalElements,
    int totalPages,
    int page,
    int size
) {
    public static OrderListResponse from(Page<OrderSummaryResponse> page) {
        return new OrderListResponse(
            page.getContent(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.getNumber(),
            page.getSize()
        );
    }
}

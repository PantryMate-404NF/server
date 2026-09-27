package com.pantrymate.pantryrecipe.pantry.infrastructure;

import com.pantrymate.common.dto.ApiResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "order-payment-service", url = "${order-payment-service.url}")
public interface OrderServiceClient {

    @GetMapping("/api/orders")
    ApiResponse<OrderListPayload> getOrders(
            @RequestHeader("X-User-Id") Long userId, @RequestParam("page") int page, @RequestParam("size") int size);

    record OrderListPayload(List<OrderSummaryPayload> content, int totalPages) {}

    record OrderSummaryPayload(String orderId, String status, LocalDateTime createdAt, List<OrderItemPayload> items) {}

    record OrderItemPayload(Long orderItemId, Long productId) {}
}

package com.pantrymate.pantryrecipe.pantry.infrastructure;

import com.pantrymate.common.dto.ApiResponse;
import com.pantrymate.pantryrecipe.pantry.domain.PurchasedOrderSource;
import com.pantrymate.pantryrecipe.pantry.infrastructure.OrderServiceClient.OrderItemPayload;
import com.pantrymate.pantryrecipe.pantry.infrastructure.OrderServiceClient.OrderListPayload;
import com.pantrymate.pantryrecipe.pantry.infrastructure.OrderServiceClient.OrderSummaryPayload;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class FeignPurchasedOrderSource implements PurchasedOrderSource {

    private static final String CONFIRMED = "CONFIRMED";
    private static final int PAGE_SIZE = 20;
    private static final int MAX_PAGES = 5;

    private final OrderServiceClient client;

    public FeignPurchasedOrderSource(OrderServiceClient client) {
        this.client = client;
    }

    @Override
    public List<PurchasedOrder> getConfirmedOrders(Long userId, LocalDateTime notBefore) {
        List<PurchasedOrder> result = new ArrayList<>();
        for (int page = 0; page < MAX_PAGES; page++) {
            ApiResponse<OrderListPayload> response = client.getOrders(userId, page, PAGE_SIZE);
            OrderListPayload payload = response == null ? null : response.data();
            if (payload == null || payload.content() == null || payload.content().isEmpty()) {
                break;
            }
            // 주문 목록은 최신순이므로 기준 시각보다 오래된 주문을 만나면 더 볼 필요가 없다.
            boolean reachedOld = false;
            for (OrderSummaryPayload order : payload.content()) {
                if (order.createdAt() == null || order.createdAt().isBefore(notBefore)) {
                    reachedOld = true;
                    continue;
                }
                if (CONFIRMED.equals(order.status())) {
                    result.add(toPurchasedOrder(order));
                }
            }
            if (reachedOld || page + 1 >= payload.totalPages()) {
                break;
            }
        }
        return result;
    }

    private PurchasedOrder toPurchasedOrder(OrderSummaryPayload order) {
        List<PurchasedItem> items = order.items() == null
                ? List.of()
                : order.items().stream()
                        .filter(item -> item.orderItemId() != null && item.productId() != null)
                        .map((OrderItemPayload item) -> new PurchasedItem(item.orderItemId(), item.productId()))
                        .toList();
        return new PurchasedOrder(order.orderId(), order.createdAt(), items);
    }
}

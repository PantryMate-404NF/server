package com.pantrymate.pantryrecipe.pantry.domain;

import java.time.LocalDateTime;
import java.util.List;

public interface PurchasedOrderSource {

    /** 결제 완료(취소·실패 제외)된 유저의 주문 중 notBefore 이후에 생성된 주문을 반환한다. */
    List<PurchasedOrder> getConfirmedOrders(Long userId, LocalDateTime notBefore);

    record PurchasedOrder(String orderId, LocalDateTime purchasedAt, List<PurchasedItem> items) {}

    record PurchasedItem(Long orderItemId, Long productId) {}
}

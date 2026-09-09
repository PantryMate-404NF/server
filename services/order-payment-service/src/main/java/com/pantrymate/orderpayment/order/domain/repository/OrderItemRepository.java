package com.pantrymate.orderpayment.order.domain.repository;

import com.pantrymate.orderpayment.order.domain.OrderItems;
import java.util.List;

public interface OrderItemRepository {

    OrderItems save(OrderItems orderItems);

    List<OrderItems> findByOrderId(Long orderId);

}

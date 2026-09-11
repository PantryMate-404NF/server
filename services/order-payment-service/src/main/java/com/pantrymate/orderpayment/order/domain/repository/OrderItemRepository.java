package com.pantrymate.orderpayment.order.domain.repository;

import com.pantrymate.orderpayment.order.domain.OrderItems;
import java.util.List;

public interface OrderItemRepository {

    OrderItems save(OrderItems orderItems);

    <S extends OrderItems> List<S> saveAll(Iterable<S> items);

    List<OrderItems> findByOrderId(Long orderId);

}

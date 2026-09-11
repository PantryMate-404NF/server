package com.pantrymate.orderpayment.order.domain.repository;

import com.pantrymate.orderpayment.order.domain.Orders;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepository {

    Orders save(Orders orders);

    Optional<Orders> findByOrderId(String orderId);

    boolean existsByIdempotencyKey(String idempotencyKey);

    Page<Orders> findByUserId(Long userId, Pageable pageable);

    Optional<Orders> findByIdempotencyKey(String idempotencyKey);

}

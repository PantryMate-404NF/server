package com.pantrymate.orderpayment.order.infrastructure.repository;

import com.pantrymate.orderpayment.order.domain.Orders;
import com.pantrymate.orderpayment.order.domain.enums.OrderStatus;
import com.pantrymate.orderpayment.order.domain.repository.OrderRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<Orders, Long>, OrderRepository {

    @Override
    Optional<Orders> findByOrderId(String orderId);

    @Override
    boolean existsByIdempotencyKey(String idempotencyKey);

    @Override
    Page<Orders> findByUserId(Long userId, Pageable pageable);

    @Override
    Page<Orders> findByUserIdAndStatusNotIn(Long userId, List<OrderStatus> status, Pageable pageable);

}

package com.pantrymate.orderpayment.order.infrastructure.repository;

import com.pantrymate.orderpayment.order.domain.OrderItems;
import com.pantrymate.orderpayment.order.domain.repository.OrderItemRepository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemJpaRepository extends JpaRepository<OrderItems, Long>,
    OrderItemRepository {

    @Override
    List<OrderItems> findByOrderId(Long orderId);
    

    
}

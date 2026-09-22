package com.pantrymate.orderpayment.cart.infrastructure.repository;

import com.pantrymate.orderpayment.cart.domain.CartItems;
import com.pantrymate.orderpayment.cart.domain.repository.CartItemRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemJpaRepository extends JpaRepository<CartItems, Long>, CartItemRepository {
    @Override
    List<CartItems> findByCartId(Long cartId);

    @Override
    Optional<CartItems> findByCartIdAndProductId(Long cartId, Long productId);

}

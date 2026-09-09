package com.pantrymate.orderpayment.cart.infrastructure.repository;

import com.pantrymate.orderpayment.cart.domain.Carts;
import com.pantrymate.orderpayment.cart.domain.repository.CartRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartJpaRepository extends JpaRepository<Carts, Long>, CartRepository {
    @Override
    Optional<Carts> findByUserId(Long userId);

}

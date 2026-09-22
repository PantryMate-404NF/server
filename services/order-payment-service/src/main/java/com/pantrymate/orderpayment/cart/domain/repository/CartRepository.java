package com.pantrymate.orderpayment.cart.domain.repository;

import com.pantrymate.orderpayment.cart.domain.Carts;
import java.util.Optional;

public interface CartRepository {
    Carts save(Carts cart);

    Optional<Carts> findByUserId(Long userId);

}

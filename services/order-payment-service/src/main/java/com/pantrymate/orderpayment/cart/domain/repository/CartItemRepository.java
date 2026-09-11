package com.pantrymate.orderpayment.cart.domain.repository;

import com.pantrymate.orderpayment.cart.domain.CartItems;
import com.pantrymate.orderpayment.cart.domain.Carts;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository {
    CartItems save(CartItems cartItems);

    void delete(CartItems cartItems);

    List<CartItems> findAllById(Iterable<Long> itemId);

    Optional<CartItems> findById(Long id);

    List<CartItems> findByCartId(Long cartId);

    Optional<CartItems> findByCartIdAndProductId(Long cartId, Long productId);


}

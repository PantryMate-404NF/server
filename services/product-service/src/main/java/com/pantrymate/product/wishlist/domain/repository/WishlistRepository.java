package com.pantrymate.product.wishlist.domain.repository;

import com.pantrymate.product.product.domain.Products;
import com.pantrymate.product.product.domain.enums.ProductStatus;
import com.pantrymate.product.wishlist.domain.Wishlists;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WishlistRepository {
    Wishlists save(Wishlists wishlists);
    boolean existsByUserIdAndProductId(Long userId,Long productId);
    long deleteByUserIdAndProductId(Long userId, Long productId);
    Page<Products> findWishedProducts(Long userId, List<ProductStatus> statuses , Pageable pageable);
    List<Long> findWishedProductIds(Long userId, List<Long> productIds);



}

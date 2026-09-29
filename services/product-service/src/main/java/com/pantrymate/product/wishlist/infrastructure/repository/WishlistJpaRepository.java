package com.pantrymate.product.wishlist.infrastructure.repository;

import com.pantrymate.product.product.domain.Products;
import com.pantrymate.product.product.domain.enums.ProductStatus;
import com.pantrymate.product.wishlist.domain.Wishlists;
import com.pantrymate.product.wishlist.domain.repository.WishlistRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WishlistJpaRepository extends JpaRepository<Wishlists, Long>, WishlistRepository {

    @Override
    @Query(value = "SELECT p FROM Products p "
        + "JOIN Wishlists w ON w.productId = p.productId "
        + "WHERE w.userId = :userId "
        + "AND p.deletedAt IS NULL "
        + "AND p.status IN :statuses "
        + "ORDER BY w.createdAt DESC ",
        countQuery = "SELECT COUNT(p) FROM Products p JOIN Wishlists w ON w.productId = p.productId "
            + "WHERE w.userId = :userId AND p.deletedAt IS NULL AND p.status IN :statuses ")
    Page<Products> findWishedProducts(@Param("userId") Long userId,
        @Param("statuses") List<ProductStatus> statuses, Pageable pageable);
    @Override
    Wishlists save(Wishlists wishlists);
    @Override
    boolean existsByUserIdAndProductId(Long userId,Long productId);
    @Override
    long deleteByUserIdAndProductId(Long userId, Long productId);
    @Override
    @Query("SELECT w.productId FROM Wishlists w "
        + "WHERE w.userId = :userId AND w.productId IN :productIds")
    List<Long> findWishedProductIds(@Param("userId") Long userId,
        @Param("productIds") List<Long> productIds);


}

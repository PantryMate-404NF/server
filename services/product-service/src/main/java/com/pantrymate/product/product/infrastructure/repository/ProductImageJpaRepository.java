package com.pantrymate.product.product.infrastructure.repository;

import com.pantrymate.product.product.domain.ProductImages;
import com.pantrymate.product.product.domain.repository.ProductImageRepository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageJpaRepository extends JpaRepository<ProductImages, Long>,
    ProductImageRepository {

    @Override
    List<ProductImages> findByProductIdOrderBySortOrderAsc(Long ProductId);
}

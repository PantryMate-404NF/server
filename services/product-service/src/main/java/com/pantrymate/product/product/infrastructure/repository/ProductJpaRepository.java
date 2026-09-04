package com.pantrymate.product.product.infrastructure.repository;

import com.pantrymate.product.product.domain.Products;
import com.pantrymate.product.product.domain.enums.ProductStatus;
import com.pantrymate.product.product.domain.repository.ProductRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface ProductJpaRepository extends JpaRepository<Products, Long>, ProductRepository {

    @Override
    Optional<Products> findBySku(String sku);

    @Override
    boolean existsBySku(String sku);

    @Override
    List<Products> findByCategoryId(Long categoryId);

    @Override
    Page<Products> findByDeletedAtIsNullAndStatusNot(ProductStatus status,  Pageable pageable);

    @Override
    Page<Products> findByCategoryIdAndDeletedAtIsNullAndStatusNot(Long CategoryId, ProductStatus status,  Pageable pageable);

}

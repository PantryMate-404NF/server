package com.pantrymate.product.product.domain.repository;

import com.pantrymate.product.product.domain.Products;
import com.pantrymate.product.product.domain.enums.ProductStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ProductRepository {
    Products save(Products product);
    Optional<Products> findBySku(String sku);
    boolean existsBySku(String sku);
    List<Products> findByCategoryId(Long categoryId);

    Page<Products> findByDeletedAtIsNullAndStatusNot(ProductStatus status, Pageable pageable);
    Page<Products> findByCategoryIdAndDeletedAtIsNullAndStatusNot(Long categoryId, ProductStatus status, Pageable pageable);
}

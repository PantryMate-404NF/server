package com.pantrymate.product.product.domain.repository;

import com.pantrymate.product.product.domain.Products;
import java.util.List;
import java.util.Optional;



public interface ProductRepository {
    Products save(Products product);
    Optional<Products> findBySku(String sku);
    boolean existsBySku(String sku);
    List<Products> findByCategoryId(Long categoryId);

}

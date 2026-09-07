package com.pantrymate.product.product.domain.repository;

import com.pantrymate.product.product.domain.ProductImages;
import java.util.List;

public interface ProductImageRepository {
    List<ProductImages> findByProductIdOrderBySortOrderAsc(Long productId);

}

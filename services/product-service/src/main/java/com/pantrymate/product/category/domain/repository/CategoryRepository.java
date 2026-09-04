package com.pantrymate.product.category.domain.repository;

import com.pantrymate.product.category.domain.Categories;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    Categories save(Categories categories);
    Optional<Categories> findById(Long id);

    boolean existsById(Long id);

    // 카테고리 depth 1에 농산, 축산, 수산, 유제품 등... 대분류
    List<Categories> findByParentIdIsNull();
    // depth 2 그에 맞는 각 상품들
    List<Categories> findByParentId(Long parentId);



}

package com.pantrymate.product.category.infrastructure.repository;

import com.pantrymate.product.category.domain.Categories;
import com.pantrymate.product.category.domain.repository.CategoryRepository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryJpaRepository extends JpaRepository<Categories, Long>, CategoryRepository {
    @Override
    List<Categories> findByParentIdIsNull();

    @Override
    List<Categories> findByParentId(Long parentId);

}

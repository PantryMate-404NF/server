package com.pantrymate.product.category.application.service;

import com.pantrymate.product.category.application.dto.CategoryChildResponse;
import com.pantrymate.product.category.application.dto.CategoryListResponse;
import com.pantrymate.product.category.application.dto.CategoryResponse;
import com.pantrymate.product.category.domain.Categories;
import com.pantrymate.product.category.domain.repository.CategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;


    @Transactional(readOnly = true)
    public CategoryListResponse getCategoryList() {
        List<Categories> topCategories = categoryRepository.findByParentIdIsNull();

        List<CategoryResponse> categoryResponse = topCategories.stream()
            .map(topCategory ->{
                List<Categories> children = categoryRepository.findByParentId(topCategory.getId());
                List<CategoryChildResponse> childResponses = children.stream()
                    .map(CategoryChildResponse::from)
                    .toList();
                    return CategoryResponse.of(topCategory, childResponses);
                })
            .toList();
        return CategoryListResponse.from(categoryResponse);
    }

}

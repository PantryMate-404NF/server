package com.pantrymate.product.category.application.dto;

import com.pantrymate.product.category.domain.Categories;
import java.util.List;

public record CategoryResponse(
    Long id,
    String name,
    List<CategoryChildResponse> children
) {
    public static CategoryResponse of(Categories category, List<CategoryChildResponse> children) {
        return new CategoryResponse(
            category.getId(),
            category.getName(),
            children
        );
    }

}

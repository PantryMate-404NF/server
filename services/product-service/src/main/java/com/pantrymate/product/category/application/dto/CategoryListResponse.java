package com.pantrymate.product.category.application.dto;

import java.util.List;

public record CategoryListResponse(
    List<CategoryResponse> categories
) {
    public static CategoryListResponse from(List<CategoryResponse> categories) {
        return new CategoryListResponse(categories);
    }

}

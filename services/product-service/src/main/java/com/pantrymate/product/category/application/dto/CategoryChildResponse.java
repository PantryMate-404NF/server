package com.pantrymate.product.category.application.dto;

import com.pantrymate.product.category.domain.Categories;

public record CategoryChildResponse(
    Long id,
    String name
) {

    public static CategoryChildResponse from(Categories category) {
        return new CategoryChildResponse(
            category.getId(),
            category.getName()
        );

    }

}

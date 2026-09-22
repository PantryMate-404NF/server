package com.pantrymate.product.product.application.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public record ProductListResponse(
    List<ProductSummaryResponse> content,
    Long totalElements,
    int totalPages
) {
    public static ProductListResponse from(Page<ProductSummaryResponse> page) {
        return new ProductListResponse(
            page.getContent(),
            page.getTotalElements(),
            page.getTotalPages()
        );
    }

}

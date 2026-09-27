package com.pantrymate.pantryrecipe.recipe.infrastructure;

import com.pantrymate.common.dto.ApiResponse;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "product-service", url = "${product-service.url}")
public interface ProductServiceClient {

    @GetMapping("/api/products")
    ApiResponse<ProductListPayload> getProducts(@RequestParam("page") int page, @RequestParam("size") int size);

    @GetMapping("/api/products/{productId}")
    ApiResponse<ProductDetailPayload> getProduct(@PathVariable("productId") Long productId);

    record ProductListPayload(List<ProductSummaryPayload> content, long totalElements, int totalPages) {}

    record ProductSummaryPayload(Long productId, String status) {}

    record ProductDetailPayload(
            Long productId,
            String name,
            Long price,
            String unit,
            Integer capacity,
            Integer packageCount,
            String thumbnailUrl,
            String status,
            Long ingredientId) {}
}

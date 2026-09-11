package com.pantrymate.orderpayment.cart.infrastructure.client;

import com.pantrymate.common.dto.ApiResponse;
import com.pantrymate.orderpayment.cart.application.dto.ProductInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", url="${product-service.url}")
public interface ProductServiceClient {
    @GetMapping("/api/products/{productId}")
    ApiResponse <ProductInfoResponse> getProductInfo(@PathVariable Long productId);

}

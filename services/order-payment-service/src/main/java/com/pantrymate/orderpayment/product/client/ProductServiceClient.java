package com.pantrymate.orderpayment.product.client;

import com.pantrymate.common.dto.ApiResponse;
import com.pantrymate.orderpayment.payment.application.dto.StockDeductionRequest;
import com.pantrymate.orderpayment.product.dto.ProductInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "product-service", url="${product-service.url}")
public interface ProductServiceClient {
    @GetMapping("/api/products/{productId}")
    ApiResponse <ProductInfoResponse> getProductInfo(@PathVariable Long productId);

    @PostMapping("/api/products/decrease")
    ApiResponse <Void> decreaseStocks(@RequestBody StockDeductionRequest request);

}

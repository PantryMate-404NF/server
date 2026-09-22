package com.pantrymate.orderpayment.product.client;

import com.pantrymate.common.dto.ApiResponse;
import com.pantrymate.orderpayment.payment.application.dto.StockDeductionRequest;
import com.pantrymate.orderpayment.payment.application.dto.StockRestoreRequest;
import com.pantrymate.orderpayment.product.dto.ProductInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
/*
            각 API 호출시 어드민 권한이 필요한 API 호출에는 아래 코드를 붙여 사용해야함.
            @RequestHeader("X-Internal-Secret") String internalSecret,

 */

@FeignClient(name = "product-service", url="${product-service.url}")
public interface ProductServiceClient {
    @GetMapping("/api/products/{productId}")
    ApiResponse <ProductInfoResponse> getProductInfo(@PathVariable Long productId);

    @PostMapping("/api/products/decrease")
    ApiResponse <Void> decreaseStocks(@RequestBody StockDeductionRequest request);

    @PostMapping("/api/products/increase")
    ApiResponse <Void> increaseStocks(@RequestBody StockRestoreRequest request);
}

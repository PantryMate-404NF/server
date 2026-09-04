package com.pantrymate.product.product.presentation.controller;

import com.pantrymate.common.dto.ApiResponse;
import com.pantrymate.product.product.application.dto.ProductRegisterRequest;
import com.pantrymate.product.product.application.dto.ProductRegisterResponse;
import com.pantrymate.product.product.application.service.ProductService;
import com.pantrymate.product.product.domain.Products;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@ResponseStatus(HttpStatus.CREATED)
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ApiResponse<ProductRegisterResponse> registerProduct(
        @Valid @RequestBody ProductRegisterRequest request) {
        Products saveProduct = productService.registerProduct(request);
        ProductRegisterResponse response = ProductRegisterResponse.from(saveProduct);

        return ApiResponse.success("상품 등록이 정상 처리되었습니다.", response);
    }
}

package com.pantrymate.product.product.presentation.controller;

import com.pantrymate.common.dto.ApiResponse;
import com.pantrymate.product.product.application.dto.ProductDetailResponse;
import com.pantrymate.product.product.application.dto.ProductListResponse;
import com.pantrymate.product.product.application.dto.ProductRegisterRequest;
import com.pantrymate.product.product.application.dto.ProductRegisterResponse;
import com.pantrymate.product.product.application.dto.ProductSummaryResponse;
import com.pantrymate.product.product.application.dto.ProductUpdateRequest;
import com.pantrymate.product.product.application.dto.ProductUpdateResponse;
import com.pantrymate.product.product.application.service.ProductService;
import com.pantrymate.product.product.domain.Products;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping
    public ApiResponse<ProductListResponse> getProductList(
        @RequestParam(required = false) Long categoryId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "60") int size) {

        Pageable pageable = PageRequest.of(page, size);
        ProductListResponse response = productService.getProductList(categoryId, pageable);
        return ApiResponse.success("상품 목록을 정상 조회하였습니다.", response);
    }

    @GetMapping("/{productId}")
    public ApiResponse<ProductDetailResponse> getProductDetail(
        @PathVariable Long productId) {

        ProductDetailResponse response = productService.getProductDetail(productId);

        return ApiResponse.success("상품 상세를 조회했습니다.", response);
    }

    @PatchMapping("/{productId}")
    public ApiResponse<ProductUpdateResponse>  updateProduct(
        @PathVariable Long productId,
        @RequestBody ProductUpdateRequest request){
        Products updatedProduct = productService.updateProduct(productId, request);
        ProductUpdateResponse response = ProductUpdateResponse.from(updatedProduct);

        return ApiResponse.success("상품 정보가 수정되었습니다.", response);
    }

}

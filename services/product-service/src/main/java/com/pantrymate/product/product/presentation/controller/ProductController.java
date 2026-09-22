package com.pantrymate.product.product.presentation.controller;

import com.pantrymate.common.dto.ApiResponse;
import com.pantrymate.common.exception.BusinessException;
import com.pantrymate.common.exception.CommonErrorCode;
import com.pantrymate.product.product.application.dto.ProductDeleteResponse;
import com.pantrymate.product.product.application.dto.ProductDetailResponse;
import com.pantrymate.product.product.application.dto.ProductDiscontinueResponse;
import com.pantrymate.product.product.application.dto.ProductListResponse;
import com.pantrymate.product.product.application.dto.ProductRegisterRequest;
import com.pantrymate.product.product.application.dto.ProductRegisterResponse;
import com.pantrymate.product.product.application.dto.ProductRestockRequest;
import com.pantrymate.product.product.application.dto.ProductRestockResponse;
import com.pantrymate.product.product.application.dto.ProductSummaryResponse;
import com.pantrymate.product.product.application.dto.ProductUpdateRequest;
import com.pantrymate.product.product.application.dto.ProductUpdateResponse;
import com.pantrymate.product.product.application.dto.StockDeductionRequest;
import com.pantrymate.product.product.application.dto.StockRestoreRequest;
import com.pantrymate.product.product.application.service.ProductService;
import com.pantrymate.product.product.domain.Products;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
public class ProductController {
    /* 어드민 권한 관련 코드 유저 도메인 담당자 요청 (백로그)
    @Value("${internal.api.secret")
    private String internalApiSecret;

    관리가 필요한 Controller의 경우
    @RequestHeader("X-Internal-Secret") String internalSecret,를 추가하여 admin 권한을
    확인 후 API를 호출할 수 있도록 함
    에러코드의 경우 아래와 같이 FORBIDDEN을 반환함
        if (!internalApiSecret.equals(internalSecret)) {
        throw new BusinessException(ProductErrorCode.FORBIDDEN);
    }
    */
    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
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

        if (page < 0 || size <= 0) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT);
        }

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
    public ApiResponse<ProductUpdateResponse> updateProduct(
        @PathVariable Long productId,
        @RequestBody ProductUpdateRequest request) {
        Products updatedProduct = productService.updateProduct(productId, request);
        ProductUpdateResponse response = ProductUpdateResponse.from(updatedProduct);

        return ApiResponse.success("상품 정보가 수정되었습니다.", response);
    }

    @PatchMapping("/{productId}/restock")
    public ApiResponse<ProductRestockResponse> restockProduct(
        @PathVariable Long productId,
        @Valid @RequestBody ProductRestockRequest request
    ){
        Products restockProduct = productService.restockProduct(productId, request.quantity());
        ProductRestockResponse response = ProductRestockResponse.from(restockProduct);

        return ApiResponse.success("수량조정이 정상적으로 완료되었습니다.", response);
    }

    @PatchMapping("/{productId}/discontinue")
    public ApiResponse<ProductDiscontinueResponse> discontinueProduct(
        @PathVariable Long productId
    ) {
        Products discontinueProduct = productService.discontinueProduct(productId);
        ProductDiscontinueResponse response = ProductDiscontinueResponse.from(discontinueProduct);
        return ApiResponse.success("상품 판매중단 요청이 정상적으로 완료되었습니다.", response);
    }

    @DeleteMapping("/{productId}")
    public ApiResponse<ProductDeleteResponse> deleteProduct(
        @PathVariable Long productId
    ) {
        Products deletedProduct = productService.deleteProduct(productId);
        ProductDeleteResponse response = ProductDeleteResponse.from(deletedProduct);
        return ApiResponse.success("상품이 삭제되었습니다.", response);
    }

    @PostMapping("/decrease")
    public ApiResponse<Void> decreaseStock(
        @RequestBody StockDeductionRequest request
    ){
        productService.productDecreaseStocks(request);
        return ApiResponse.success("재고 차감에 성공하였습니다.");
    }

    @PostMapping("/increase")
    public ApiResponse<Void> increaseStock(
        @RequestBody StockRestoreRequest request
    ){
        productService.productIncreaseStocks(request);
        return ApiResponse.success("재고 복구에 성공하였습니다.");
    }

}

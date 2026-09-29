package com.pantrymate.product.wishlist.presentation;

import com.pantrymate.common.dto.ApiResponse;
import com.pantrymate.common.dto.CurrentUser;
import com.pantrymate.common.exception.BusinessException;
import com.pantrymate.common.exception.CommonErrorCode;
import com.pantrymate.product.product.application.dto.ProductListResponse;
import com.pantrymate.product.wishlist.application.dto.WishedProductsResponse;
import com.pantrymate.product.wishlist.application.service.WishListService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wishlists")
@RequiredArgsConstructor
public class WishlistController {
    private final WishListService wishlistService;

    @PostMapping("/{productId}")
    public ApiResponse<Void> addWishlist(
        CurrentUser currentUser,
        @PathVariable Long productId
    ){
        wishlistService.addWishlist(currentUser.userId(), productId);
        return ApiResponse.success("찜 등록이 완료되었습니다.");
    }
    @DeleteMapping("/{productId}")
    public ApiResponse<Void> deleteWishlist(
        CurrentUser currentUser,
        @PathVariable Long productId
    ){
        wishlistService.deleteWishlist(currentUser.userId(), productId);
        return ApiResponse.success("찜 해제가 완료되었습니다.");
    }
    @GetMapping
    public ApiResponse<ProductListResponse> getWishlist(
        CurrentUser currentUser,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "60") int size
    ){
        if(page < 0 || size <= 0){
            throw new BusinessException(CommonErrorCode.INVALID_INPUT);
        }
        Pageable pageable = PageRequest.of(page, size);
        ProductListResponse response = wishlistService.getWishlist(currentUser.userId(), pageable);
        return ApiResponse.success("정상적으로 찜목록 조회가 완료되었습니다",  response);
    }

    @GetMapping("/status")
    public ApiResponse<WishedProductsResponse> getWishedStatus(
        CurrentUser currentUser,
        @RequestParam(required = false) List<Long> productIds
    ) {
        if (productIds != null && productIds.size() > 100) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT);
        }
        WishedProductsResponse response = wishlistService.getWishedProductIds(currentUser.userId(), productIds);
        return ApiResponse.success("찜 여부 조회가 완료되었습니다.", response);
    }
}

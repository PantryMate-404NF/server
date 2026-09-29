package com.pantrymate.product.wishlist.application.service;

import com.pantrymate.common.exception.BusinessException;
import com.pantrymate.product.product.application.dto.ProductListResponse;
import com.pantrymate.product.product.application.dto.ProductSummaryResponse;
import com.pantrymate.product.product.domain.Products;
import com.pantrymate.product.product.domain.enums.ProductStatus;
import com.pantrymate.product.product.domain.exception.ProductErrorCode;
import com.pantrymate.product.product.domain.repository.ProductRepository;
import com.pantrymate.product.wishlist.application.dto.WishedProductsResponse;
import com.pantrymate.product.wishlist.domain.Wishlists;
import com.pantrymate.product.wishlist.domain.repository.WishlistRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WishListService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;


    public void addWishlist(Long userId, Long productId) {
        Products product = productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND));
        if (product.isDeleted()) {
            throw new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND);
        }
        if (wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            return;
        }

        try {
            wishlistRepository.save(Wishlists.create(userId, productId));
        } catch (DataIntegrityViolationException e) {
            // 동시 요청으로 다른 스레드가 먼저 저장함 → 이미 찜된 상태이므로 성공으로 처리
        }

    }

    @Transactional
    public void deleteWishlist(Long userId, Long productId) {
        wishlistRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Transactional(readOnly = true)
    public ProductListResponse getWishlist(Long userId, Pageable pageable) {
        List<ProductStatus> statuses = List.of(ProductStatus.ON_SALE, ProductStatus.OUT_OF_STOCK);
        Page<Products> wishPage = wishlistRepository.findWishedProducts(userId, statuses, pageable);
        Page<ProductSummaryResponse> summaryPage = wishPage.map(ProductSummaryResponse::from);
        return ProductListResponse.from(summaryPage);
    }

    @Transactional(readOnly = true)
    public WishedProductsResponse getWishedProductIds(Long userId, List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return new WishedProductsResponse(List.of());
        }
        List<Long> wished = wishlistRepository.findWishedProductIds(userId, productIds);
        return new WishedProductsResponse(wished);
    }
}

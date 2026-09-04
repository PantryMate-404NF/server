package com.pantrymate.product.product.application.service;

import com.pantrymate.common.exception.BusinessException;
import com.pantrymate.product.category.domain.exception.CategoryErrorCode;
import com.pantrymate.product.category.domain.repository.CategoryRepository;
import com.pantrymate.product.product.application.dto.ProductRegisterRequest;
import com.pantrymate.product.product.domain.Products;
import com.pantrymate.product.product.domain.enums.ProductStatus;
import com.pantrymate.product.product.domain.enums.ProductUnit;
import com.pantrymate.product.product.domain.exception.ProductErrorCode;
import com.pantrymate.product.product.domain.repository.ProductImageRepository;
import com.pantrymate.product.product.domain.repository.ProductRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public Products registerProduct(ProductRegisterRequest request) {
        if (productRepository.existsBySku(request.sku())) {
            throw new BusinessException(ProductErrorCode.DUPLICATE_SKU);
        }
        if(!categoryRepository.existsById(request.categoryId()))
            throw new BusinessException(CategoryErrorCode.CATEGORY_NOT_FOUND);
        if(request.thumbnailUrl()==null || request.thumbnailUrl().isBlank())
            throw new BusinessException(ProductErrorCode.THUMBNAIL_REQUIRED);
        if(request.price() == null || request.price() <= 0){
            throw new BusinessException(ProductErrorCode.INVALID_PRICE);
        }

        Products product = buildNewProduct(request);
        return productRepository.save(product);
    }

    private Products buildNewProduct(ProductRegisterRequest request) {
        LocalDateTime now = LocalDateTime.now();
        ProductStatus initialStatus = (request.stockQuantity() == 0)
            ? ProductStatus.OUT_OF_STOCK : ProductStatus.ON_SALE;
        return Products.builder()
            .sku(request.sku())
            .name(request.name())
            .categoryId(request.categoryId())
            .price(request.price())
            .unit(ProductUnit.valueOf(request.unit()))
            .capacity(request.capacity())
            .packageCount(request.packageCount())
            .origin(request.origin())
            .description(request.description())
            .thumbnailUrl(request.thumbnailUrl())
            .ingredientId(request.ingredientId())
            .stockQuantity(request.stockQuantity())
            .status(initialStatus)
            .createdAt(now)
            .updatedAt(now)
            .build();
    }


}

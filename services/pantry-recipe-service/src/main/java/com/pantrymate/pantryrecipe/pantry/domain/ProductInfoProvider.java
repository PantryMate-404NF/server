package com.pantrymate.pantryrecipe.pantry.domain;

import com.pantrymate.pantryrecipe.ingredient.domain.enums.StorageType;
import java.util.Optional;

public interface ProductInfoProvider {

    Optional<ProductInfo> getProductInfo(Long productId);

    record ProductInfo(Long productId, String name, String thumbnailUrl, Long ingredientId, StorageType storageType) {}
}

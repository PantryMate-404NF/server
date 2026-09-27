package com.pantrymate.pantryrecipe.pantry.domain;

import java.util.Optional;

public interface ProductInfoProvider {

    Optional<ProductInfo> getProductInfo(Long productId);

    record ProductInfo(Long productId, String name, String thumbnailUrl, Long ingredientId) {}
}

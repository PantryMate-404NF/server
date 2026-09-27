package com.pantrymate.pantryrecipe.pantry.infrastructure;

import com.pantrymate.common.dto.ApiResponse;
import com.pantrymate.pantryrecipe.pantry.domain.ProductInfoProvider;
import com.pantrymate.pantryrecipe.recipe.infrastructure.ProductServiceClient;
import com.pantrymate.pantryrecipe.recipe.infrastructure.ProductServiceClient.ProductDetailPayload;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** 구매 당시 상품 정보(식재료 매핑)를 조회한다. 품절·판매종료 상품도 조회 대상이며 성공 결과만 캐시한다. */
@Component
public class FeignProductInfoProvider implements ProductInfoProvider {

    private static final Logger log = LoggerFactory.getLogger(FeignProductInfoProvider.class);
    private static final Duration TTL = Duration.ofMinutes(30);

    private record Cached(ProductInfo info, Instant loadedAt) {}

    private final ProductServiceClient client;
    private final Map<Long, Cached> cache = new ConcurrentHashMap<>();

    public FeignProductInfoProvider(ProductServiceClient client) {
        this.client = client;
    }

    @Override
    public Optional<ProductInfo> getProductInfo(Long productId) {
        Cached cached = cache.get(productId);
        if (cached != null && cached.loadedAt().plus(TTL).isAfter(Instant.now())) {
            return Optional.of(cached.info());
        }
        try {
            ApiResponse<ProductDetailPayload> response = client.getProduct(productId);
            ProductDetailPayload detail = response == null ? null : response.data();
            if (detail == null) {
                return Optional.empty();
            }
            ProductInfo info =
                    new ProductInfo(detail.productId(), detail.name(), detail.thumbnailUrl(), detail.ingredientId());
            cache.put(productId, new Cached(info, Instant.now()));
            return Optional.of(info);
        } catch (RuntimeException e) {
            log.warn("상품 정보 조회 실패(productId={}): {}", productId, e.getMessage());
            return Optional.empty();
        }
    }
}

package com.pantrymate.pantryrecipe.recipe.infrastructure;

import com.pantrymate.common.dto.ApiResponse;
import com.pantrymate.pantryrecipe.recipe.domain.ProductCandidate;
import com.pantrymate.pantryrecipe.recipe.infrastructure.ProductServiceClient.ProductDetailPayload;
import com.pantrymate.pantryrecipe.recipe.infrastructure.ProductServiceClient.ProductListPayload;
import com.pantrymate.pantryrecipe.recipe.infrastructure.ProductServiceClient.ProductSummaryPayload;
import com.pantrymate.pantryrecipe.recipe.domain.ProductCatalog;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 상품 서비스의 기존 목록/상세 API만으로 식재료 매핑 상품 후보를 모아 짧게 캐시한다.
 * 상세(용량·식재료 매핑)는 자주 바뀌지 않아 더 오래 캐시하고, 판매 상태는 목록 조회로 매번 갱신한다.
 */
@Component
public class FeignProductCatalog implements ProductCatalog {

    private static final Logger log = LoggerFactory.getLogger(FeignProductCatalog.class);
    private static final String ON_SALE = "ON_SALE";
    private static final int PAGE_SIZE = 100;
    private static final Duration DETAIL_TTL = Duration.ofMinutes(30);

    private record CachedDetail(ProductDetailPayload detail, Instant loadedAt) {}

    private record Snapshot(List<ProductCandidate> candidates, Instant loadedAt) {}

    private final ProductServiceClient client;
    private final Duration snapshotTtl;
    private final Map<Long, CachedDetail> detailCache = new ConcurrentHashMap<>();
    private volatile Snapshot snapshot = new Snapshot(List.of(), Instant.EPOCH);

    public FeignProductCatalog(
            ProductServiceClient client, @Value("${product.catalog.cache-ttl-seconds}") long cacheTtlSeconds) {
        this.client = client;
        this.snapshotTtl = Duration.ofSeconds(cacheTtlSeconds);
    }

    @Override
    public List<ProductCandidate> getOnSaleCandidates() {
        Snapshot current = snapshot;
        if (isFresh(current)) {
            return current.candidates();
        }
        return refresh();
    }

    private boolean isFresh(Snapshot target) {
        return target.loadedAt().plus(snapshotTtl).isAfter(Instant.now());
    }

    private synchronized List<ProductCandidate> refresh() {
        Snapshot current = snapshot;
        if (isFresh(current)) {
            return current.candidates();
        }
        try {
            List<ProductCandidate> candidates = load();
            snapshot = new Snapshot(candidates, Instant.now());
            return candidates;
        } catch (RuntimeException e) {
            log.warn("상품 서비스 조회 실패, 마지막 성공 결과를 사용한다: {}", e.getMessage());
            snapshot = new Snapshot(current.candidates(), Instant.now());
            return current.candidates();
        }
    }

    private List<ProductCandidate> load() {
        Set<Long> onSaleIds = fetchOnSaleProductIds();
        detailCache.keySet().retainAll(onSaleIds);

        List<ProductCandidate> candidates = new ArrayList<>();
        for (Long productId : onSaleIds) {
            ProductDetailPayload detail = getDetail(productId);
            if (detail != null
                    && ON_SALE.equals(detail.status())
                    && detail.ingredientId() != null) {
                candidates.add(new ProductCandidate(
                        detail.ingredientId(),
                        detail.productId(),
                        detail.name(),
                        detail.price(),
                        detail.thumbnailUrl(),
                        detail.unit(),
                        detail.capacity(),
                        detail.packageCount()));
            }
        }
        return List.copyOf(candidates);
    }

    private Set<Long> fetchOnSaleProductIds() {
        Set<Long> ids = new java.util.LinkedHashSet<>();
        int page = 0;
        int totalPages = 1;
        while (page < totalPages) {
            ApiResponse<ProductListPayload> response = client.getProducts(page, PAGE_SIZE);
            ProductListPayload payload = response == null ? null : response.data();
            if (payload == null) {
                // 일부 페이지만 받은 결과가 정상 스냅샷으로 저장되지 않도록 실패로 처리해 마지막 성공 결과를 유지한다.
                throw new IllegalStateException("상품 목록 응답이 비어 있음(page=" + page + ")");
            }
            totalPages = payload.totalPages();
            ids.addAll(payload.content().stream()
                    .filter(product -> ON_SALE.equals(product.status()))
                    .map(ProductSummaryPayload::productId)
                    .collect(Collectors.toList()));
            page++;
        }
        return ids;
    }

    private ProductDetailPayload getDetail(Long productId) {
        CachedDetail cached = detailCache.get(productId);
        if (cached != null && cached.loadedAt().plus(DETAIL_TTL).isAfter(Instant.now())) {
            return cached.detail();
        }
        try {
            ApiResponse<ProductDetailPayload> response = client.getProduct(productId);
            ProductDetailPayload detail = response == null ? null : response.data();
            if (detail != null) {
                detailCache.put(productId, new CachedDetail(detail, Instant.now()));
            }
            return detail;
        } catch (RuntimeException e) {
            log.warn("상품 상세 조회 실패(productId={}): {}", productId, e.getMessage());
            return cached == null ? null : cached.detail();
        }
    }
}

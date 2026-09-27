package com.pantrymate.pantryrecipe.pantry.application;

import com.pantrymate.pantryrecipe.ingredient.domain.Ingredient;
import com.pantrymate.pantryrecipe.ingredient.domain.IngredientRepository;
import com.pantrymate.pantryrecipe.ingredient.domain.enums.StorageType;
import com.pantrymate.pantryrecipe.pantry.domain.PantryItem;
import com.pantrymate.pantryrecipe.pantry.domain.PantryItemRepository;
import com.pantrymate.pantryrecipe.pantry.domain.ProductInfoProvider;
import com.pantrymate.pantryrecipe.pantry.domain.ProductInfoProvider.ProductInfo;
import com.pantrymate.pantryrecipe.pantry.domain.PurchasedOrderSource;
import com.pantrymate.pantryrecipe.pantry.domain.PurchasedOrderSource.PurchasedItem;
import com.pantrymate.pantryrecipe.pantry.domain.PurchasedOrderSource.PurchasedOrder;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * 자사몰 구매 상품의 팬트리 자동 등록(PANTRY-001).
 * 배송 도메인이 생기기 전까지는 결제 완료 후 일정 시간(delivery-delay-minutes)이 지난 주문을 배송 완료로 간주하며,
 * 유저가 팬트리를 조회하는 시점에 그 유저의 주문만 확인해 등록한다. order_item_id로 중복 등록을 막는다.
 */
@Service
public class DeliveryAutoRegisterService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryAutoRegisterService.class);

    private final PurchasedOrderSource purchasedOrderSource;
    private final ProductInfoProvider productInfoProvider;
    private final IngredientRepository ingredientRepository;
    private final PantryItemRepository pantryItemRepository;
    private final boolean enabled;
    private final Duration deliveryDelay;
    private final Duration lookback;
    private final Duration minSyncInterval;
    private final int fallbackExtraDays;
    private final Map<Long, Instant> lastSyncedAt = new ConcurrentHashMap<>();

    public DeliveryAutoRegisterService(
            PurchasedOrderSource purchasedOrderSource,
            ProductInfoProvider productInfoProvider,
            IngredientRepository ingredientRepository,
            PantryItemRepository pantryItemRepository,
            @Value("${pantry.auto-register.enabled}") boolean enabled,
            @Value("${pantry.auto-register.delivery-delay-minutes}") long deliveryDelayMinutes,
            @Value("${pantry.auto-register.lookback-days}") long lookbackDays,
            @Value("${pantry.auto-register.min-sync-interval-seconds}") long minSyncIntervalSeconds,
            @Value("${pantry.expiry.fallback-extra-days}") int fallbackExtraDays) {
        this.purchasedOrderSource = purchasedOrderSource;
        this.productInfoProvider = productInfoProvider;
        this.ingredientRepository = ingredientRepository;
        this.pantryItemRepository = pantryItemRepository;
        this.enabled = enabled;
        this.deliveryDelay = Duration.ofMinutes(deliveryDelayMinutes);
        this.lookback = Duration.ofDays(lookbackDays);
        this.minSyncInterval = Duration.ofSeconds(minSyncIntervalSeconds);
        this.fallbackExtraDays = fallbackExtraDays;
    }

    /** 실패해도 호출한 요청에는 영향을 주지 않는다(주문·조회 흐름과 분리). 다음 조회 때 다시 시도된다. */
    public void syncUser(Long userId) {
        if (!enabled || !claimSyncSlot(userId)) {
            return;
        }
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime deliveredBefore = now.minus(deliveryDelay);
            for (PurchasedOrder order : purchasedOrderSource.getConfirmedOrders(userId, now.minus(lookback))) {
                if (order.purchasedAt().isAfter(deliveredBefore)) {
                    continue;
                }
                for (PurchasedItem item : order.items()) {
                    registerItem(userId, order, item);
                }
            }
        } catch (RuntimeException e) {
            log.warn("팬트리 자동 등록 동기화 실패 userId={}: {}", userId, e.getMessage());
        }
    }

    private boolean claimSyncSlot(Long userId) {
        Instant now = Instant.now();
        boolean[] claimed = {false};
        lastSyncedAt.compute(userId, (key, previous) -> {
            if (previous == null || previous.plus(minSyncInterval).isBefore(now)) {
                claimed[0] = true;
                return now;
            }
            return previous;
        });
        return claimed[0];
    }

    private void registerItem(Long userId, PurchasedOrder order, PurchasedItem item) {
        if (pantryItemRepository.existsByOrderItemId(item.orderItemId())) {
            return;
        }
        Optional<ProductInfo> product = productInfoProvider.getProductInfo(item.productId());
        if (product.isEmpty() || product.get().ingredientId() == null) {
            log.warn(
                    "팬트리 자동 등록 제외(식재료 매핑 없음) orderId={} orderItemId={} productId={}",
                    order.orderId(),
                    item.orderItemId(),
                    item.productId());
            return;
        }
        Ingredient ingredient =
                ingredientRepository.findById(product.get().ingredientId()).orElse(null);
        if (ingredient == null) {
            log.warn(
                    "팬트리 자동 등록 제외(식재료 사전에 없음) orderItemId={} ingredientId={}",
                    item.orderItemId(),
                    product.get().ingredientId());
            return;
        }

        LocalDate purchaseDate = order.purchasedAt().toLocalDate();
        LocalDate sellByDate = ingredient.getDefaultShelfLifeDays() == null
                ? null
                : purchaseDate.plusDays(ingredient.getDefaultShelfLifeDays());
        int extraDays = ingredient.getExtendedConsumptionDays() == null
                ? fallbackExtraDays
                : ingredient.getExtendedConsumptionDays();
        LocalDate expiryDate = (sellByDate == null ? purchaseDate : sellByDate).plusDays(extraDays);
        StorageType storageType =
                ingredient.getDefaultStorageType() == null ? StorageType.REFRIGERATED : ingredient.getDefaultStorageType();

        try {
            pantryItemRepository.save(PantryItem.createAuto(
                    userId,
                    ingredient,
                    ingredient.getName(),
                    product.get().thumbnailUrl() != null ? product.get().thumbnailUrl() : ingredient.getImageUrl(),
                    storageType,
                    purchaseDate,
                    sellByDate,
                    expiryDate,
                    item.orderItemId()));
        } catch (DataIntegrityViolationException e) {
            log.debug("이미 자동 등록된 주문 상품 orderItemId={}", item.orderItemId());
        }
    }
}

package com.pantrymate.pantryrecipe.pantry.application;

import com.pantrymate.common.exception.BusinessException;
import com.pantrymate.pantryrecipe.ingredient.domain.Ingredient;
import com.pantrymate.pantryrecipe.ingredient.domain.IngredientRepository;
import com.pantrymate.pantryrecipe.ingredient.domain.enums.StorageType;
import com.pantrymate.pantryrecipe.pantry.domain.PantryItem;
import com.pantrymate.pantryrecipe.pantry.domain.PantryItemRepository;
import com.pantrymate.pantryrecipe.pantry.domain.enums.PantryRegisterType;
import com.pantrymate.pantryrecipe.pantry.domain.enums.PantrySortType;
import com.pantrymate.pantryrecipe.pantry.domain.exception.PantryErrorCode;
import com.pantrymate.pantryrecipe.pantry.presentation.dto.PantryItemCreateRequestDto;
import com.pantrymate.pantryrecipe.pantry.presentation.dto.PantryItemResponseDto;
import com.pantrymate.pantryrecipe.pantry.presentation.dto.PantryItemUpdateRequestDto;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PantryItemService {

    private static final int MAX_NAME_LENGTH = 20;

    private final PantryItemRepository pantryItemRepository;
    private final IngredientRepository ingredientRepository;
    private final int defaultFallbackExtensionDays;
    private final int tempSellByToExpiryDays;

    public PantryItemService(
            PantryItemRepository pantryItemRepository,
            IngredientRepository ingredientRepository,
            @Value("${pantry.expiry.default-fallback-extension-days}") int defaultFallbackExtensionDays,
            @Value("${pantry.expiry.temp-sell-by-to-expiry-days}") int tempSellByToExpiryDays) {
        this.pantryItemRepository = pantryItemRepository;
        this.ingredientRepository = ingredientRepository;
        this.defaultFallbackExtensionDays = defaultFallbackExtensionDays;
        this.tempSellByToExpiryDays = tempSellByToExpiryDays;
    }

    @Transactional
    public PantryItemResponseDto save(Long userId, PantryItemCreateRequestDto request) {
        String name = validateName(request.ingredientName());
        StorageType storageType = validateStorageType(request.storageType());
        ResolvedExpiry resolved = resolveExpiry(request.expiryDate(), request.sellByDate());
        Ingredient ingredient = matchIngredient(name);

        PantryItem pantryItem = PantryItem.createManual(
                userId,
                ingredient,
                name,
                request.imageUrl(),
                storageType,
                resolved.sellByDate(),
                resolved.expiryDate(),
                resolved.autoCalculated());
        PantryItem saved = pantryItemRepository.save(pantryItem);
        return PantryItemResponseDto.from(saved);
    }

    @Transactional(readOnly = true)
    public List<PantryItemResponseDto> getAll(Long userId, String rawStorageType, String rawSort) {
        StorageType storageType = rawStorageType == null || rawStorageType.isBlank() ? null : validateStorageType(rawStorageType);
        PantrySortType sort = validateSort(rawSort);

        List<PantryItem> items =
                switch (sort) {
                    case RECENT -> storageType == null
                            ? pantryItemRepository.findByUserIdOrderByCreatedAtDesc(userId)
                            : pantryItemRepository.findByUserIdAndStorageTypeOrderByCreatedAtDesc(userId, storageType);
                    case OLDEST -> storageType == null
                            ? pantryItemRepository.findByUserIdOrderByCreatedAtAsc(userId)
                            : pantryItemRepository.findByUserIdAndStorageTypeOrderByCreatedAtAsc(userId, storageType);
                    case IMMINENT -> storageType == null
                            ? pantryItemRepository.findByUserIdOrderByImminent(userId)
                            : pantryItemRepository.findByUserIdAndStorageTypeOrderByImminent(userId, storageType);
                };
        return items.stream().map(PantryItemResponseDto::from).toList();
    }

    @Transactional
    public PantryItemResponseDto update(Long userId, Long pantryItemId, PantryItemUpdateRequestDto request) {
        PantryItem pantryItem = getByIdAndUserId(pantryItemId, userId);

        ResolvedExpiry resolved = resolveExpiry(request.expiryDate(), request.sellByDate());

        if (pantryItem.getRegisterType() == PantryRegisterType.MANUAL) {
            String name = validateName(request.ingredientName());
            StorageType storageType = validateStorageType(request.storageType());
            Ingredient ingredient = matchIngredient(name);
            pantryItem.updateManualFields(
                    ingredient,
                    name,
                    request.imageUrl(),
                    storageType,
                    resolved.sellByDate(),
                    resolved.expiryDate(),
                    resolved.autoCalculated());
        } else {
            // 자사몰 연동(자동 등록) 식재료는 식재료명·보관방법·이미지가 SKU에 연결되어 있어 수정 대상에서 제외한다.
            pantryItem.updateExpiryDate(resolved.sellByDate(), resolved.expiryDate(), resolved.autoCalculated());
        }

        if (request.cookable() != null) {
            pantryItem.updateCookable(request.cookable());
        }

        return PantryItemResponseDto.from(pantryItem);
    }

    @Transactional(readOnly = true)
    public List<Long> getUserIdsWithItems() {
        return pantryItemRepository.findDistinctUserIds();
    }

    @Transactional
    public void delete(Long userId, Long pantryItemId) {
        PantryItem pantryItem = getByIdAndUserId(pantryItemId, userId);
        pantryItemRepository.delete(pantryItem);
    }

    @Transactional
    public void deleteAllByUser(Long userId, List<Long> pantryItemIds) {
        List<PantryItem> items = pantryItemRepository.findAllById(pantryItemIds);
        boolean allOwnedByUser =
                items.size() == pantryItemIds.size() && items.stream().allMatch(item -> item.getUserId().equals(userId));
        if (!allOwnedByUser) {
            throw new BusinessException(PantryErrorCode.PANTRY_NOTFOUND_ITEM);
        }
        pantryItemRepository.deleteAll(items);
    }

    private PantryItem getByIdAndUserId(Long pantryItemId, Long userId) {
        return pantryItemRepository
                .findById(pantryItemId)
                .filter(item -> item.getUserId().equals(userId))
                .orElseThrow(() -> new BusinessException(PantryErrorCode.PANTRY_NOTFOUND_ITEM));
    }

    private String validateName(String rawName) {
        if (rawName == null || rawName.isBlank() || rawName.length() > MAX_NAME_LENGTH) {
            throw new BusinessException(PantryErrorCode.PANTRY_INVALID_NAME);
        }
        return rawName;
    }

    private StorageType validateStorageType(String rawStorageType) {
        if (rawStorageType == null) {
            throw new BusinessException(PantryErrorCode.PANTRY_INVALID_STORAGE);
        }
        try {
            return StorageType.valueOf(rawStorageType);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(PantryErrorCode.PANTRY_INVALID_STORAGE);
        }
    }

    private PantrySortType validateSort(String rawSort) {
        if (rawSort == null || rawSort.isBlank()) {
            return PantrySortType.RECENT;
        }
        try {
            return PantrySortType.valueOf(rawSort);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(PantryErrorCode.PANTRY_INVALID_SORT);
        }
    }

    private ResolvedExpiry resolveExpiry(String rawExpiryDate, String rawSellByDate) {
        if (rawExpiryDate != null && !rawExpiryDate.isBlank()) {
            LocalDate expiryDate = parseDate(rawExpiryDate);
            LocalDate sellByDate = rawSellByDate == null || rawSellByDate.isBlank() ? null : parseDate(rawSellByDate);
            if (sellByDate != null && sellByDate.isAfter(expiryDate)) {
                throw new BusinessException(PantryErrorCode.PANTRY_INVALID_DATE);
            }
            return new ResolvedExpiry(sellByDate, expiryDate, false);
        }
        if (rawSellByDate != null && !rawSellByDate.isBlank()) {
            LocalDate sellByDate = parseDate(rawSellByDate);
            // TODO: Ingredient.extendedConsumptionDays로 식재료별 소비기한 연장일을 조회해 반영. 매칭 전까지는 임시로 고정일수만 더한다.
            return new ResolvedExpiry(sellByDate, sellByDate.plusDays(tempSellByToExpiryDays), true);
        }
        return new ResolvedExpiry(null, LocalDate.now().plusDays(defaultFallbackExtensionDays), true);
    }

    private Ingredient matchIngredient(String name) {
        return ingredientRepository.findByNameIgnoreCase(name).orElse(null);
    }

    private LocalDate parseDate(String rawDate) {
        try {
            return LocalDate.parse(rawDate);
        } catch (DateTimeParseException e) {
            throw new BusinessException(PantryErrorCode.PANTRY_INVALID_DATE);
        }
    }

    private record ResolvedExpiry(LocalDate sellByDate, LocalDate expiryDate, boolean autoCalculated) {}
}

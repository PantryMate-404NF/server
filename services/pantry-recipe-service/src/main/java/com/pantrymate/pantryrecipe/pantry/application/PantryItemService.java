package com.pantrymate.pantryrecipe.pantry.application;

import com.pantrymate.common.exception.BusinessException;
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
    private final int defaultFallbackExtensionDays;

    public PantryItemService(
            PantryItemRepository pantryItemRepository,
            @Value("${pantry.expiry.default-fallback-extension-days}") int defaultFallbackExtensionDays) {
        this.pantryItemRepository = pantryItemRepository;
        this.defaultFallbackExtensionDays = defaultFallbackExtensionDays;
    }

    @Transactional
    public PantryItemResponseDto save(Long userId, PantryItemCreateRequestDto request) {
        String name = validateName(request.ingredientName());
        StorageType storageType = validateStorageType(request.storageType());
        boolean expiryAutoCalculated = request.expiryDate() == null || request.expiryDate().isBlank();
        LocalDate expiryDate = resolveExpiryDate(request.expiryDate(), expiryAutoCalculated);

        PantryItem pantryItem =
                PantryItem.createManual(userId, name, request.imageUrl(), storageType, expiryDate, expiryAutoCalculated);
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

        boolean expiryAutoCalculated = request.expiryDate() == null || request.expiryDate().isBlank();
        LocalDate expiryDate = resolveExpiryDate(request.expiryDate(), expiryAutoCalculated);

        if (pantryItem.getRegisterType() == PantryRegisterType.MANUAL) {
            String name = validateName(request.ingredientName());
            StorageType storageType = validateStorageType(request.storageType());
            pantryItem.updateManualFields(name, request.imageUrl(), storageType, expiryDate, expiryAutoCalculated);
        } else {
            // 자사몰 연동(자동 등록) 식재료는 식재료명·보관방법·이미지가 SKU에 연결되어 있어 수정 대상에서 제외한다.
            pantryItem.updateExpiryDate(expiryDate, expiryAutoCalculated);
        }

        if (request.cookable() != null) {
            pantryItem.updateCookable(request.cookable());
        }

        return PantryItemResponseDto.from(pantryItem);
    }

    @Transactional
    public void delete(Long userId, Long pantryItemId) {
        PantryItem pantryItem = getByIdAndUserId(pantryItemId, userId);
        pantryItemRepository.delete(pantryItem);
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

    private LocalDate resolveExpiryDate(String rawExpiryDate, boolean expiryAutoCalculated) {
        if (expiryAutoCalculated) {
            return LocalDate.now().plusDays(defaultFallbackExtensionDays);
        }
        try {
            return LocalDate.parse(rawExpiryDate);
        } catch (DateTimeParseException e) {
            throw new BusinessException(PantryErrorCode.PANTRY_INVALID_DATE);
        }
    }
}

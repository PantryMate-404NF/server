package com.pantrymate.pantryrecipe.pantry.application;

import com.pantrymate.common.exception.BusinessException;
import com.pantrymate.pantryrecipe.ingredient.domain.enums.StorageType;
import com.pantrymate.pantryrecipe.pantry.domain.PantryItem;
import com.pantrymate.pantryrecipe.pantry.domain.PantryItemRepository;
import com.pantrymate.pantryrecipe.pantry.domain.exception.PantryErrorCode;
import com.pantrymate.pantryrecipe.pantry.presentation.dto.PantryItemCreateRequestDto;
import com.pantrymate.pantryrecipe.pantry.presentation.dto.PantryItemResponseDto;
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
    public List<PantryItemResponseDto> getAll(Long userId, String rawStorageType) {
        List<PantryItem> items;
        if (rawStorageType != null && !rawStorageType.isBlank()) {
            StorageType storageType = validateStorageType(rawStorageType);
            items = pantryItemRepository.findByUserIdAndStorageTypeOrderByCreatedAtDesc(userId, storageType);
        } else {
            items = pantryItemRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }
        return items.stream().map(PantryItemResponseDto::from).toList();
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

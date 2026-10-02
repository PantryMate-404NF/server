package com.pantrymate.product.product.domain.enums;

import com.pantrymate.common.exception.BusinessException;
import com.pantrymate.product.product.domain.exception.ProductErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StorageType {
    REFRIGERATED("냉장"),
    FROZEN("냉동"),
    ROOM_TEMP("실온");
    private final String label;
    public static StorageType from(String storageType) {
        try{
            return StorageType.valueOf(storageType);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ProductErrorCode.INVALID_STORAGE_TYPE);
        }
    }

}

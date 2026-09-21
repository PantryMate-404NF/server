package com.pantrymate.product.product.domain.enums;

import com.pantrymate.common.exception.BusinessException;
import com.pantrymate.product.product.domain.exception.ProductErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductUnit {
    GRAM("g"),
    KILOGRAM("kg"),
    MILLILITER("ml"),
    LITER("l"),
    EACH("e");
    private final String displayValue;
    public static ProductUnit from(String unit){
        try{
            return ProductUnit.valueOf(unit);
        } catch (IllegalArgumentException e){
            throw new BusinessException(ProductErrorCode.INVALID_UNIT);
        }
    }
}

package com.pantrymate.product.product.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductStatus {
    ON_SALE("판매중", "정상적으로 판매중인 상품입니다."),
    OUT_OF_STOCK("품절", "일시적으로 재고가 없는 상품입니다."),
    DISCONTINUED("판매중단", "판매가 중단된 상품입니다.");

    private final String label;
    private final String description;

    /*상태전이규칙 사용  ON_SALE -> OUT_OF_STOCK || DISCONTINUED -> ON_SALE || DISCONTINUED (True)
     DISCONTINUED -> ON_SALE || OUT_OF_STOCK (FALSE)를 사용하여 상품 판매에 대한 혼란을 방지함.*/

    public boolean canTransitionTo(ProductStatus status) {
        return switch (this) {
            case ON_SALE -> status == OUT_OF_STOCK || status == DISCONTINUED;
            case OUT_OF_STOCK -> status == ON_SALE || status == DISCONTINUED;
            case DISCONTINUED -> false;
        };
    }

}


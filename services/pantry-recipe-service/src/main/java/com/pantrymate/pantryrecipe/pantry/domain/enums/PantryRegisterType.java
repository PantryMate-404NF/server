package com.pantrymate.pantryrecipe.pantry.domain.enums;

public enum PantryRegisterType {
    MANUAL("사용자 등록"),
    AUTO("자사몰 구매");

    private final String label;

    PantryRegisterType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

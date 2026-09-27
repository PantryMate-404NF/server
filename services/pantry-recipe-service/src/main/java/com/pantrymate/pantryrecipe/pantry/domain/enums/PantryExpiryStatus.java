package com.pantrymate.pantryrecipe.pantry.domain.enums;

public enum PantryExpiryStatus {
    NORMAL,
    IMMINENT,
    EXPIRED;

    private static final long IMMINENT_THRESHOLD_DAYS = 3;

    public static PantryExpiryStatus fromRemainingDays(long dDay) {
        if (dDay < 0) {
            return EXPIRED;
        }
        if (dDay < IMMINENT_THRESHOLD_DAYS) {
            return IMMINENT;
        }
        return NORMAL;
    }
}

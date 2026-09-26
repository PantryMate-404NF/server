package com.pantrymate.pantryrecipe.recipe.domain;

import java.util.List;

public interface UserAllergyPort {

    /** 온보딩에서 저장한 알레르기 라벨. 온보딩 정보가 없으면 빈 목록이며, 조회하지 못하면 예외를 던진다. */
    List<String> getAllergies(Long userId);

    class UserAllergyUnavailableException extends RuntimeException {
        public UserAllergyUnavailableException(String message) {
            super(message);
        }
    }
}

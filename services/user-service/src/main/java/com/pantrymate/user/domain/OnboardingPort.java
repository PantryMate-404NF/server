package com.pantrymate.user.domain;

import java.util.List;

/** AI 서버와의 온보딩 연동(화면에 보여줄 음식 목록 조회, 온보딩 결과 전달). */
public interface OnboardingPort {

    PresentedFoods getPresentedFoods();

    void saveOnboarding(OnboardingSnapshot snapshot);

    record PresentedFoods(int listVersion, List<Food> items) {}

    record Food(String name, String family) {}

    /** 저장된 온보딩 설정 중 AI 취향 계산에 쓰는 값. 음식 유형은 우리 코드(KOREAN 등) 그대로 담는다. */
    record OnboardingSnapshot(
            Long userId,
            List<String> picks,
            Integer spicy,
            Integer salty,
            Integer sweet,
            List<String> preferredFoodTypes,
            List<String> allergies,
            Integer householdSize) {}

    class OnboardingUnavailableException extends RuntimeException {
        public OnboardingUnavailableException(String message) {
            super(message);
        }
    }
}

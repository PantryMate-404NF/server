package com.pantrymate.user.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record UserPreferenceUpdateRequestDto(
        @Schema(description = "가족 구성원 수 (1~20, 필수)", minimum = "1", maximum = "20") Integer familyMemberCount,
        @Schema(
                        description = "선호 음식 유형 (최대 5개, 중복 불가). 생략/null/빈 배열은 모두 '값 없음'으로 저장됨(기존 값 유지 아님)",
                        allowableValues = {"KOREAN", "WESTERN", "JAPANESE", "CHINESE", "ETC"})
                List<String> preferredFoodTypes,
        @Schema(description = "알레르기 유발 식재료명 (최대 20개, 중복 불가, 자유 텍스트). 생략/null/빈 배열은 모두 '값 없음'으로 저장됨")
                List<String> allergies,
        @Schema(description = "온보딩 완료 여부 (필수)") Boolean onboardingCompleted,
        @Schema(description = "온보딩 진행 단계 (1부터 시작, 필수)", minimum = "1") Integer onboardingStep) {
}

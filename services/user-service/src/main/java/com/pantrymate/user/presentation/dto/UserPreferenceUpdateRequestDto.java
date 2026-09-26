package com.pantrymate.user.presentation.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "완전 교체(full replace) 방식 — 생략/null/빈 배열은 모두 '값 없음'으로 저장되며 기존 값이 유지되지 않는다.")
public record UserPreferenceUpdateRequestDto(
        @Schema(
                        description = "가족 구성원 수",
                        example = "3",
                        minimum = "1",
                        maximum = "10",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                Integer familyMemberCount,
        @ArraySchema(
                        schema =
                                @Schema(
                                        description = "생략/null/빈 배열은 모두 '값 없음'으로 저장됨(기존 값 유지 아님)",
                                        allowableValues = {"KOREAN", "WESTERN", "JAPANESE", "CHINESE", "ETC"}),
                        maxItems = 5,
                        uniqueItems = true)
                List<String> preferredFoodTypes,
        @ArraySchema(
                        schema =
                                @Schema(
                                        description = "알레르기 유발 식품(정해진 19종 중 선택). 생략/null/빈 배열은 모두 '값 없음'으로 저장됨",
                                        example = "새우",
                                        allowableValues = {
                                            "알류(가금류)", "우유", "메밀", "땅콩", "대두", "밀", "고등어", "게", "새우", "돼지고기",
                                            "복숭아", "토마토", "아황산류", "호두", "닭고기", "쇠고기", "오징어",
                                            "조개류(굴,전복,홍합 포함)", "잣"
                                        }),
                        maxItems = 19,
                        uniqueItems = true)
                List<String> allergies,
        @ArraySchema(
                        schema =
                                @Schema(
                                        description = "좋아하는 음식 (자유 텍스트). 최소 3개 이상 선택해야 하며, "
                                                + "생략/null/빈 배열은 아직 이 단계에 도달하지 않은 것으로 보고 '값 없음'으로 저장됨",
                                        example = "김치찌개"),
                        minItems = 3,
                        maxItems = 10,
                        uniqueItems = true)
                List<String> favoriteFoods,
        @Schema(
                        description = "짠맛/단맛/매운맛 선호도 (각 1~5단계). 생략/null이면 아직 이 단계에 도달하지 않은 것으로 보고 "
                                + "'값 없음'으로 저장되며, 값을 보낼 경우 세 항목 모두 채워야 함")
                TastePreferenceDto tastePreferences,
        @Schema(description = "온보딩 완료 여부", requiredMode = Schema.RequiredMode.REQUIRED) Boolean onboardingCompleted,
        @Schema(
                        description = "온보딩 진행 단계 (1부터 시작)",
                        example = "2",
                        minimum = "1",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                Integer onboardingStep) {
}

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
                        maximum = "20",
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
                                        description = "알레르기 유발 식재료명 (자유 텍스트). 생략/null/빈 배열은 모두 '값 없음'으로 저장됨",
                                        example = "갑각류"),
                        maxItems = 20,
                        uniqueItems = true)
                List<String> allergies,
        @Schema(description = "온보딩 완료 여부", requiredMode = Schema.RequiredMode.REQUIRED) Boolean onboardingCompleted,
        @Schema(
                        description = "온보딩 진행 단계 (1부터 시작)",
                        example = "2",
                        minimum = "1",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                Integer onboardingStep) {
}

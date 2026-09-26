package com.pantrymate.user.presentation.dto;

import com.pantrymate.user.domain.OnboardingPort.PresentedFoods;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record OnboardingFoodListResponseDto(
        @Schema(description = "목록 버전. 목록은 바뀔 수 있으니 화면을 그리기 직전에 조회한다", example = "2") int listVersion,
        List<Item> items) {

    @Schema(name = "OnboardingFoodItem")
    public record Item(
            @Schema(description = "음식 이름. 선택 결과는 이 이름 그대로 favoriteFoods에 담아 보낸다", example = "오징어볶음") String name,
            @Schema(description = "음식 계열", example = "korean") String family) {}

    public static OnboardingFoodListResponseDto from(PresentedFoods foods) {
        return new OnboardingFoodListResponseDto(
                foods.listVersion(),
                foods.items().stream().map(f -> new Item(f.name(), f.family())).toList());
    }
}

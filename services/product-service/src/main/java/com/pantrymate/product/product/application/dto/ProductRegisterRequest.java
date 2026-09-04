package com.pantrymate.product.product.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductRegisterRequest(
    @NotBlank
    String sku,

    @NotBlank(message = "이름은 필수 값 입니다.")
    String name,

    @NotNull
    Long categoryId,

    //가격은 0보다 무조건 커야함.
    @NotNull
    @Positive
    Long price,

    @NotNull
    String unit,

    Integer capacity,

    Integer packageCount,

    String origin,

    String description,

    @NotBlank(message = "대표 이미지 URL은 필수 값입니다.")
    String thumbnailUrl,

    Long ingredientId,

    //재고는 0개를 허용하므로 0이상이여야한다.
    @NotNull
    @PositiveOrZero
    Integer stockQuantity


) {

}

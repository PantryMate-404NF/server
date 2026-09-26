package com.pantrymate.user.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserAddressCreateRequestDto(
        @Schema(description = "수령자 이름 (1~50자)", example = "윤모씨", requiredMode = Schema.RequiredMode.REQUIRED)
                String recipientName,
        @Schema(
                        description = "수령자 휴대폰 번호. 하이픈은 있어도 되며 숫자만 저장된다",
                        example = "010-1234-5678",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                String recipientPhone,
        @Schema(description = "우편번호 (5자리)", example = "12345", requiredMode = Schema.RequiredMode.REQUIRED)
                String zipCode,
        @Schema(
                        description = "기본 주소 (우편번호 서비스 검색 결과, 200자 이내)",
                        example = "경기도 성남시 분당구 불정로 90",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                String address,
        @Schema(description = "상세 주소 (200자 이내)", example = "101동 1001호", nullable = true) String addressDetail,
        @Schema(description = "기본 배송지로 설정할지 여부. 첫 배송지는 값과 무관하게 기본 배송지가 된다", example = "false")
                Boolean isDefault) {}

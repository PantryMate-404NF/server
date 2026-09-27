package com.pantrymate.user.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "완전 교체(full replace) 방식 — 모든 필수 항목을 다시 보내야 한다. 기본 배송지 변경은 별도 API를 사용한다.")
public record UserAddressUpdateRequestDto(
        @Schema(description = "수령자 이름 (1~50자)", example = "윤모씨", requiredMode = Schema.RequiredMode.REQUIRED)
                String recipientName,
        @Schema(
                        description = "수령자 휴대폰 번호. 하이픈은 있어도 되며 숫자만 저장된다",
                        example = "010-1234-5678",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                String recipientPhone,
        @Schema(description = "우편번호 (5자리)", example = "13606", requiredMode = Schema.RequiredMode.REQUIRED)
                String zipCode,
        @Schema(description = "기본 주소 (200자 이내)", example = "경기도 성남시 분당구 불정로 90", requiredMode = Schema.RequiredMode.REQUIRED)
                String address,
        @Schema(description = "상세 주소 (200자 이내)", example = "101동 1001호", nullable = true) String addressDetail) {}

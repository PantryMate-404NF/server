package com.pantrymate.user.presentation.dto;

import com.pantrymate.user.domain.UserAddress;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserAddressResponseDto(
        @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED) Long addressId,
        @Schema(example = "윤모씨", requiredMode = Schema.RequiredMode.REQUIRED) String recipientName,
        @Schema(description = "숫자만 저장된 휴대폰 번호", example = "01012345678", requiredMode = Schema.RequiredMode.REQUIRED)
                String recipientPhone,
        @Schema(example = "12345", requiredMode = Schema.RequiredMode.REQUIRED) String zipCode,
        @Schema(example = "경기도 성남시 분당구 불정로 90", requiredMode = Schema.RequiredMode.REQUIRED) String address,
        @Schema(example = "101동 1001호", nullable = true) String addressDetail,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean isDefault) {

    public static UserAddressResponseDto from(UserAddress userAddress) {
        return new UserAddressResponseDto(
                userAddress.getAddressId(),
                userAddress.getRecipientName(),
                userAddress.getRecipientPhone(),
                userAddress.getZipCode(),
                userAddress.getAddress(),
                userAddress.getAddressDetail(),
                userAddress.isDefaultAddress());
    }
}

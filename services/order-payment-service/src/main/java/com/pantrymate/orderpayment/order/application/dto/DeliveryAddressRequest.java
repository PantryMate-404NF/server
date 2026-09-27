package com.pantrymate.orderpayment.order.application.dto;

import jakarta.validation.constraints.NotBlank;

public record DeliveryAddressRequest(
    @NotBlank
    String recipientName,
    @NotBlank
    String recipientPhone,
    @NotBlank
    String zipCode,
    @NotBlank
    String address,
    String addressDetail
) {

}

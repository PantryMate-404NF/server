package com.pantrymate.notification.devicetoken.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record DeviceTokenRegisterRequestDto(
        @Schema(
                        description = "FCM 기기 토큰",
                        example = "dQw4w9WgXcQ:APA91bF...",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                String fcmToken) {}

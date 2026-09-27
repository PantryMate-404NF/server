package com.pantrymate.notification.devicetoken.presentation.controller;

import com.pantrymate.common.dto.ApiResponse;
import com.pantrymate.common.dto.CurrentUser;
import com.pantrymate.notification.devicetoken.application.DeviceTokenService;
import com.pantrymate.notification.devicetoken.presentation.dto.DeviceTokenRegisterRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "NOTIFICATION", description = "알림")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/notifications")
public class DeviceTokenController {

    private final DeviceTokenService deviceTokenService;

    public DeviceTokenController(DeviceTokenService deviceTokenService) {
        this.deviceTokenService = deviceTokenService;
    }

    @Operation(summary = "FCM 기기 토큰 등록", description = "로그인한 유저의 FCM 기기 토큰을 등록/갱신한다. 유저당 토큰 1개만 유지한다(재등록 시 덮어씀).")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "등록 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "DEVICE-TOKEN-INVALID"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUTH-UNAUTHORIZED")
    })
    @PostMapping("/device-token")
    public ResponseEntity<ApiResponse<Void>> register(
            @Parameter(hidden = true) CurrentUser currentUser, @RequestBody DeviceTokenRegisterRequestDto request) {
        deviceTokenService.register(currentUser.userId(), request.fcmToken());
        return ResponseEntity.ok(ApiResponse.success("기기 토큰이 등록되었습니다.", null));
    }
}

package com.pantrymate.user.presentation.controller;

import com.pantrymate.common.dto.ApiResponse;
import com.pantrymate.common.dto.CurrentUser;
import com.pantrymate.user.application.UserService;
import com.pantrymate.user.presentation.dto.UserPreferenceResponseDto;
import com.pantrymate.user.presentation.dto.UserPreferenceUpdateRequestDto;
import com.pantrymate.user.presentation.dto.UserProfileResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "USER", description = "회원 프로필 · 개인화 온보딩")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "내 프로필 조회", description = "로그인된 유저의 기본 회원 정보와 온보딩 완료 여부를 조회한다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUTH-UNAUTHORIZED"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "USER-NOTFOUND-ID")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<UserProfileResponseDto>> getProfile(
            @Parameter(hidden = true) CurrentUser currentUser) {
        UserProfileResponseDto profile = userService.getProfile(currentUser.userId());
        return ResponseEntity.ok(ApiResponse.success("프로필 조회가 완료되었습니다.", profile));
    }

    @Operation(
            summary = "개인화 온보딩 설정 등록/수정",
            description = "가족 구성원 수, 선호 음식 유형, 알레르기 식재료, 온보딩 진행 단계/완료 여부를 등록·수정한다. "
                    + "완전 교체(full replace) 방식 — 생략/null/빈 배열은 모두 '값 없음'으로 저장되며 기존 값이 유지되지 않는다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "저장 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "ONBOARD-INVALID-INPUT — familyMemberCount(1~20)/onboardingStep(1 이상) 누락 또는 범위 초과, "
                        + "preferredFoodTypes 허용값 외 값·중복·5개 초과, allergies 중복·20개 초과"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUTH-UNAUTHORIZED"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "USER-NOTFOUND-ID")
    })
    @PutMapping("/preferences")
    public ResponseEntity<ApiResponse<UserPreferenceResponseDto>> updatePreferences(
            @Parameter(hidden = true) CurrentUser currentUser,
            @RequestBody UserPreferenceUpdateRequestDto request) {
        UserPreferenceResponseDto response = userService.savePreferences(currentUser.userId(), request);
        return ResponseEntity.ok(ApiResponse.success("개인화 온보딩 설정이 저장되었습니다.", response));
    }
}

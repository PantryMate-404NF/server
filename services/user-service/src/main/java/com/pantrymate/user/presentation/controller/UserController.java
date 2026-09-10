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
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "Bearer 토큰 누락/무효 — API Gateway 단에서 차단되어 이 서비스까지 도달하지 않고, "
                        + "공통 응답 규격이 아닌 Gateway의 기본 401(바디 없음)이 내려간다. "
                        + "AUTH-UNAUTHORIZED는 X-User-Id 헤더 없이 이 서비스가 직접 호출된 경우에만 발생하는 내부 폴백 코드."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "USER-NOTFOUND-ID")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<UserProfileResponseDto>> getProfile(
            @Parameter(hidden = true) CurrentUser currentUser) {
        UserProfileResponseDto profile = userService.getProfile(currentUser.userId());
        return ResponseEntity.ok(ApiResponse.success("프로필 조회가 완료되었습니다.", profile));
    }

    @Operation(
            summary = "개인화 온보딩 설정 조회",
            description = "저장된 개인화 온보딩 설정과 마지막 진행 단계를 조회한다. "
                    + "온보딩 중 건너뛰기/강제종료로 이탈한 사용자가 마이페이지에서 재진입할 때, 반환된 onboardingStep부터 이어서 진행한다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "Bearer 토큰 누락/무효 — API Gateway 단에서 차단되어 이 서비스까지 도달하지 않고, "
                        + "공통 응답 규격이 아닌 Gateway의 기본 401(바디 없음)이 내려간다. "
                        + "AUTH-UNAUTHORIZED는 X-User-Id 헤더 없이 이 서비스가 직접 호출된 경우에만 발생하는 내부 폴백 코드."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "USER-NOTFOUND-ID / ONBOARD-NOTFOUND-PREFERENCE — 저장된 온보딩 설정이 없음(온보딩을 시작조차 하지 않은 사용자)")
    })
    @GetMapping("/preferences")
    public ResponseEntity<ApiResponse<UserPreferenceResponseDto>> getPreferences(
            @Parameter(hidden = true) CurrentUser currentUser) {
        UserPreferenceResponseDto response = userService.getPreferences(currentUser.userId());
        return ResponseEntity.ok(ApiResponse.success("개인화 온보딩 설정 조회가 완료되었습니다.", response));
    }

    @Operation(
            summary = "개인화 온보딩 설정 등록/수정",
            description = "가족 구성원 수, 선호 음식 유형, 알레르기 식재료, 좋아하는 음식, 맛 선호도, "
                    + "온보딩 진행 단계/완료 여부를 등록·수정한다. "
                    + "완전 교체(full replace) 방식 — 생략/null/빈 배열은 모두 '값 없음'으로 저장되며 기존 값이 유지되지 않는다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "저장 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "ONBOARD-INVALID-INPUT — familyMemberCount(1~20)/onboardingStep(1 이상) 누락 또는 범위 초과, "
                        + "preferredFoodTypes 허용값 외 값·중복·5개 초과, allergies 중복·20개 초과, "
                        + "favoriteFoods 3개 미만(비어있지 않은 경우)·중복·10개 초과, "
                        + "tastePreferences 값 지정 시 salty/sweet/spicy 중 1~5 범위를 벗어나거나 누락된 항목 존재"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "Bearer 토큰 누락/무효 — API Gateway 단에서 차단되어 이 서비스까지 도달하지 않고, "
                        + "공통 응답 규격이 아닌 Gateway의 기본 401(바디 없음)이 내려간다. "
                        + "AUTH-UNAUTHORIZED는 X-User-Id 헤더 없이 이 서비스가 직접 호출된 경우에만 발생하는 내부 폴백 코드."),
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

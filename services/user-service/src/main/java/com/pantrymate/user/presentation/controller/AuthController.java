package com.pantrymate.user.presentation.controller;

import com.pantrymate.common.dto.ApiResponse;
import com.pantrymate.common.exception.BusinessException;
import com.pantrymate.user.application.AuthService;
import com.pantrymate.user.application.LoginResult;
import com.pantrymate.user.application.ReissueResult;
import com.pantrymate.user.domain.exception.UserErrorCode;
import com.pantrymate.user.infrastructure.jwt.JwtProvider;
import com.pantrymate.user.presentation.dto.ReissueResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@Tag(name = "AUTH", description = "소셜 로그인 · 토큰 재발급 · 로그아웃")
@RestController
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    private static final String OAUTH_STATE_COOKIE = "oauthState";
    private static final long OAUTH_STATE_MAX_AGE_SECONDS = 300;

    private final AuthService authService;
    private final JwtProvider jwtProvider;
    private final String frontendCallbackUrl;

    public AuthController(
            AuthService authService,
            JwtProvider jwtProvider,
            @Value("${app.frontend-callback-url}") String frontendCallbackUrl) {
        this.authService = authService;
        this.jwtProvider = jwtProvider;
        this.frontendCallbackUrl = frontendCallbackUrl;
    }

    @Operation(
            summary = "소셜 로그인 시작",
            description = "FE 로그인 버튼이 호출하는 엔드포인트. 카카오/네이버 인가 코드 요청 URL로 302 리다이렉트한다. "
                    + "카카오/네이버 REST API 키는 서버에서만 사용하며 FE에는 노출되지 않는다. "
                    + "로그인 CSRF 방지를 위해 state를 발급해 HttpOnly 쿠키로 심는다.",
            security = {})
    @GetMapping("/api/auth/authorize/{provider}")
    public ResponseEntity<Void> authorize(
            @Parameter(description = "소셜 제공자", example = "kakao") @PathVariable String provider) {
        String state = UUID.randomUUID().toString();
        String authorizeUrl = authService.getAuthorizeUrl(provider, state);
        ResponseCookie stateCookie = buildStateCookie(state, OAUTH_STATE_MAX_AGE_SECONDS);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(authorizeUrl))
                .header(HttpHeaders.SET_COOKIE, stateCookie.toString())
                .build();
    }

    @Operation(
            summary = "소셜 로그인 및 간편 가입",
            description = "카카오/네이버 OAuth Redirect URI로 직접 등록되는 엔드포인트. "
                    + "authorize 단계에서 발급된 state를 쿠키 값과 대조해 로그인 CSRF를 차단한 뒤, "
                    + "인가코드 검증 후 로그인/자동가입 처리하고, Refresh Token을 HttpOnly 쿠키로 심어 FE 콜백으로 302 리다이렉트한다. "
                    + "Access Token은 이 응답에 포함되지 않으며 /auth/reissue로 별도 발급받아야 한다.",
            security = {})
    @GetMapping("/api/auth/login/{provider}")
    public ResponseEntity<Void> login(
            @Parameter(description = "소셜 제공자", example = "kakao") @PathVariable String provider,
            @Parameter(description = "소셜 서버가 전달하는 OAuth2 인가 코드")
                    @RequestParam(value = "code", required = false) String code,
            @Parameter(description = "소셜 서버가 그대로 반환하는 CSRF 방지용 state")
                    @RequestParam(value = "state", required = false) String state,
            @Parameter(hidden = true) @CookieValue(value = OAUTH_STATE_COOKIE, required = false) String stateCookie) {
        ResponseCookie expiredStateCookie = buildStateCookie("", 0);
        try {
            if (!StringUtils.hasText(code)) {
                throw new BusinessException(UserErrorCode.AUTH_INVALID_AUTH_CODE);
            }
            if (!StringUtils.hasText(state) || !StringUtils.hasText(stateCookie) || !state.equals(stateCookie)) {
                throw new BusinessException(UserErrorCode.AUTH_INVALID_STATE);
            }
            LoginResult result = authService.login(provider, code);
            ResponseCookie cookie = buildRefreshCookie(result.refreshToken(), jwtProvider.getRefreshTokenExpirySeconds());
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(frontendCallbackUrl))
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .header(HttpHeaders.SET_COOKIE, expiredStateCookie.toString())
                    .build();
        } catch (BusinessException e) {
            URI redirectUri = UriComponentsBuilder.fromUriString(frontendCallbackUrl)
                    .queryParam("error", e.getErrorCode().getCode())
                    .build()
                    .toUri();
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(redirectUri)
                    .header(HttpHeaders.SET_COOKIE, expiredStateCookie.toString())
                    .build();
        }
    }

    @Operation(
            summary = "토큰 재발급",
            description = "HttpOnly refreshToken 쿠키를 검증해 Access Token을 새로 발급한다. "
                    + "Refresh Token Rotation(RTR) 적용 - 호출 시마다 Refresh Token도 새로 발급되어 쿠키가 갱신된다.",
            security = {})
    @PostMapping("/api/auth/reissue")
    public ResponseEntity<ApiResponse<ReissueResponseDto>> reissue(
            @Parameter(hidden = true) @CookieValue(value = REFRESH_TOKEN_COOKIE, required = false) String refreshToken) {
        ReissueResult result = authService.reissue(refreshToken);
        ResponseCookie cookie = buildRefreshCookie(result.refreshToken(), jwtProvider.getRefreshTokenExpirySeconds());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success("토큰이 성공적으로 재발급되었습니다.", new ReissueResponseDto(result.accessToken())));
    }

    @Operation(
            summary = "로그아웃",
            description = "Redis에 저장된 Refresh Token을 삭제하고 refreshToken 쿠키를 만료시킨다. Bearer Access Token 필요.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/api/auth/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Parameter(hidden = true) @CookieValue(value = REFRESH_TOKEN_COOKIE, required = false) String refreshToken) {
        authService.logout(refreshToken);
        ResponseCookie expiredCookie = buildRefreshCookie("", 0);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
                .body(ApiResponse.success("로그아웃이 성공적으로 완료되었습니다.", null));
    }

    private ResponseCookie buildRefreshCookie(String value, long maxAgeSeconds) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }

    private ResponseCookie buildStateCookie(String value, long maxAgeSeconds) {
        return ResponseCookie.from(OAUTH_STATE_COOKIE, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(maxAgeSeconds)
                .build();
    }
}

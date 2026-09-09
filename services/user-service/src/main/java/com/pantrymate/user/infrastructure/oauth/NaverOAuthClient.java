package com.pantrymate.user.infrastructure.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pantrymate.common.exception.BusinessException;
import com.pantrymate.user.domain.AuthProvider;
import com.pantrymate.user.domain.exception.UserErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class NaverOAuthClient implements SocialOAuthClient {

    private final RestClient restClient = RestClient.create();

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public NaverOAuthClient(
            @Value("${oauth.naver.client-id}") String clientId,
            @Value("${oauth.naver.client-secret}") String clientSecret,
            @Value("${oauth.naver.redirect-uri}") String redirectUri) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    @Override
    public AuthProvider supports() {
        return AuthProvider.NAVER;
    }

    @Override
    public String buildAuthorizeUrl(String state) {
        // 네이버는 카카오와 달리 state가 필수 파라미터다. AuthController가 발급한 CSRF 방지용 state를 그대로 전달한다.
        return UriComponentsBuilder.fromUriString("https://nid.naver.com/oauth2.0/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", state)
                .build()
                .toUriString();
    }

    @Override
    public SocialUserProfile fetchProfile(String authorizationCode) {
        NaverTokenResponse token = exchangeToken(authorizationCode);
        return fetchUserInfo(token.accessToken());
    }

    private NaverTokenResponse exchangeToken(String code) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("nid.naver.com")
                            .path("/oauth2.0/token")
                            .queryParam("grant_type", "authorization_code")
                            .queryParam("client_id", clientId)
                            .queryParam("client_secret", clientSecret)
                            .queryParam("code", code)
                            .build())
                    .retrieve()
                    .body(NaverTokenResponse.class);
        } catch (RestClientResponseException e) {
            throw mapException(e);
        } catch (RestClientException e) {
            throw new BusinessException(UserErrorCode.AUTH_OAUTH_COMMUNICATION_ERROR);
        }
    }

    private SocialUserProfile fetchUserInfo(String accessToken) {
        try {
            NaverUserResponse response = restClient.get()
                    .uri("https://openapi.naver.com/v1/nid/me")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(NaverUserResponse.class);

            NaverProfile profile = response.response();
            String nickname = StringUtils.hasText(profile.nickname()) ? profile.nickname() : profile.name();

            return new SocialUserProfile(
                    profile.id(), profile.email(), StringUtils.hasText(nickname) ? nickname : "네이버사용자", profile.profileImage());
        } catch (RestClientException e) {
            throw new BusinessException(UserErrorCode.AUTH_OAUTH_COMMUNICATION_ERROR);
        }
    }

    private BusinessException mapException(RestClientResponseException e) {
        if (e.getStatusCode().is4xxClientError()) {
            return new BusinessException(UserErrorCode.AUTH_INVALID_AUTH_CODE);
        }
        return new BusinessException(UserErrorCode.AUTH_OAUTH_COMMUNICATION_ERROR);
    }

    private record NaverTokenResponse(@JsonProperty("access_token") String accessToken) {
    }

    private record NaverUserResponse(String resultcode, String message, NaverProfile response) {
    }

    private record NaverProfile(
            String id, String email, String name, String nickname, @JsonProperty("profile_image") String profileImage) {
    }
}

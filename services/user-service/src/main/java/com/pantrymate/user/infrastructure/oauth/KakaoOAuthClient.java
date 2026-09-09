package com.pantrymate.user.infrastructure.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pantrymate.common.exception.BusinessException;
import com.pantrymate.user.domain.AuthProvider;
import com.pantrymate.user.domain.exception.UserErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class KakaoOAuthClient implements SocialOAuthClient {

    private final RestClient restClient = RestClient.create();

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public KakaoOAuthClient(
            @Value("${oauth.kakao.client-id}") String clientId,
            @Value("${oauth.kakao.client-secret:}") String clientSecret,
            @Value("${oauth.kakao.redirect-uri}") String redirectUri) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    @Override
    public AuthProvider supports() {
        return AuthProvider.KAKAO;
    }

    @Override
    public String buildAuthorizeUrl(String state) {
        return UriComponentsBuilder.fromUriString("https://kauth.kakao.com/oauth/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", state)
                .build()
                .toUriString();
    }

    @Override
    public SocialUserProfile fetchProfile(String authorizationCode) {
        KakaoTokenResponse token = exchangeToken(authorizationCode);
        return fetchUserInfo(token.accessToken());
    }

    private KakaoTokenResponse exchangeToken(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", clientId);
        if (StringUtils.hasText(clientSecret)) {
            form.add("client_secret", clientSecret);
        }
        form.add("redirect_uri", redirectUri);
        form.add("code", code);

        try {
            return restClient.post()
                    .uri("https://kauth.kakao.com/oauth/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(KakaoTokenResponse.class);
        } catch (RestClientResponseException e) {
            throw mapException(e);
        } catch (RestClientException e) {
            throw new BusinessException(UserErrorCode.AUTH_OAUTH_COMMUNICATION_ERROR);
        }
    }

    private SocialUserProfile fetchUserInfo(String accessToken) {
        try {
            KakaoUserResponse response = restClient.get()
                    .uri("https://kapi.kakao.com/v2/user/me")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(KakaoUserResponse.class);

            KakaoAccount account = response.kakaoAccount();
            KakaoProfile profile = account != null ? account.profile() : null;

            String email = account != null ? account.email() : null;
            String nickname = profile != null ? profile.nickname() : null;
            String profileImageUrl = profile != null ? profile.profileImageUrl() : null;

            return new SocialUserProfile(
                    String.valueOf(response.id()), email, StringUtils.hasText(nickname) ? nickname : "카카오사용자", profileImageUrl);
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

    private record KakaoTokenResponse(@JsonProperty("access_token") String accessToken) {
    }

    private record KakaoUserResponse(Long id, @JsonProperty("kakao_account") KakaoAccount kakaoAccount) {
    }

    private record KakaoAccount(String email, KakaoProfile profile) {
    }

    private record KakaoProfile(String nickname, @JsonProperty("profile_image_url") String profileImageUrl) {
    }
}

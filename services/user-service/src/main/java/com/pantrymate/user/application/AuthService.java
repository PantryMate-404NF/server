package com.pantrymate.user.application;

import com.pantrymate.common.exception.BusinessException;
import com.pantrymate.user.domain.AuthProvider;
import com.pantrymate.user.domain.User;
import com.pantrymate.user.domain.UserRepository;
import com.pantrymate.user.domain.exception.UserErrorCode;
import com.pantrymate.user.infrastructure.jwt.JwtProvider;
import com.pantrymate.user.infrastructure.jwt.RefreshTokenRepository;
import com.pantrymate.user.infrastructure.oauth.SocialOAuthClient;
import com.pantrymate.user.infrastructure.oauth.SocialOAuthClientResolver;
import com.pantrymate.user.infrastructure.oauth.SocialUserProfile;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuthService {

    private final SocialOAuthClientResolver oauthClientResolver;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthService(
            SocialOAuthClientResolver oauthClientResolver,
            UserRepository userRepository,
            JwtProvider jwtProvider,
            RefreshTokenRepository refreshTokenRepository) {
        this.oauthClientResolver = oauthClientResolver;
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public LoginResult login(String provider, String authorizationCode) {
        SocialOAuthClient client = oauthClientResolver.resolve(provider);
        SocialUserProfile profile = client.fetchProfile(authorizationCode);

        User user = getOrCreateUser(client.supports(), profile);

        String refreshToken = jwtProvider.generateRefreshToken(user.getUserId());
        refreshTokenRepository.save(user.getUserId(), refreshToken, jwtProvider.getRefreshTokenExpirySeconds());

        return new LoginResult(refreshToken);
    }

    @Transactional(readOnly = true)
    public ReissueResult reissue(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            throw new BusinessException(UserErrorCode.AUTH_MISSING_TOKEN);
        }

        Long userId = parseUserId(refreshToken);
        User user = getUserByIdOrThrow(userId);

        String storedToken = refreshTokenRepository
                .find(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.AUTH_EXPIRED_TOKEN));

        if (!storedToken.equals(refreshToken)) {
            // 저장된 토큰과 다른 토큰이 재사용됨 = 탈취 의심. 해당 유저의 세션을 전부 무효화한다.
            refreshTokenRepository.delete(userId);
            throw new BusinessException(UserErrorCode.AUTH_INVALID_TOKEN);
        }

        String newAccessToken = jwtProvider.generateAccessToken(userId, user.getRole());
        String newRefreshToken = jwtProvider.generateRefreshToken(userId);
        refreshTokenRepository.save(userId, newRefreshToken, jwtProvider.getRefreshTokenExpirySeconds());

        return new ReissueResult(newAccessToken, newRefreshToken);
    }

    public void logout(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            return;
        }
        try {
            Long userId = jwtProvider.getUserId(refreshToken);
            refreshTokenRepository.delete(userId);
        } catch (JwtException | IllegalArgumentException e) {
            // 이미 만료/위조된 토큰이면 조용히 무시한다. 로그아웃은 항상 성공 처리.
        }
    }

    private User getOrCreateUser(AuthProvider provider, SocialUserProfile profile) {
        return userRepository
                .findByProviderAndProviderId(provider, profile.providerId())
                .orElseGet(() -> userRepository.save(User.createSocialUser(
                        provider, profile.providerId(), profile.email(), profile.nickname(), profile.profileImageUrl())));
    }

    private User getUserByIdOrThrow(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOTFOUND_ID));
    }

    private Long parseUserId(String refreshToken) {
        try {
            return jwtProvider.getUserId(refreshToken);
        } catch (ExpiredJwtException e) {
            throw new BusinessException(UserErrorCode.AUTH_EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(UserErrorCode.AUTH_INVALID_TOKEN);
        }
    }
}

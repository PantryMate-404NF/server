package com.pantrymate.user.infrastructure.jwt;

import com.pantrymate.user.domain.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * gateway-service와 동일한 security.jwt.secret(HS256)으로 서명한다.
 * gateway는 이 토큰을 자체 검증하고 X-User-Id 헤더로 전파하며, user-service는 토큰을 재검증하지 않는다.
 */
@Component
public class JwtProvider {

    private final SecretKey key;
    private final long accessTokenExpirySeconds;
    private final long refreshTokenExpirySeconds;

    public JwtProvider(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access-token-expiry-seconds}") long accessTokenExpirySeconds,
            @Value("${security.jwt.refresh-token-expiry-seconds}") long refreshTokenExpirySeconds) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirySeconds = accessTokenExpirySeconds;
        this.refreshTokenExpirySeconds = refreshTokenExpirySeconds;
    }

    public String generateAccessToken(Long userId, UserRole role) {
        return buildToken(userId, Map.of("role", role.name()), accessTokenExpirySeconds);
    }

    public String generateRefreshToken(Long userId) {
        return buildToken(userId, Map.of(), refreshTokenExpirySeconds);
    }

    public long getRefreshTokenExpirySeconds() {
        return refreshTokenExpirySeconds;
    }

    public Long getUserId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    private String buildToken(Long userId, Map<String, ?> claims, long expirySeconds) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirySeconds)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}

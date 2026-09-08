package com.pantrymate.user.infrastructure.jwt;

import java.time.Duration;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * Refresh Token Rotation(RTR)을 위해 userId 당 최신 Refresh Token 1개만 유지한다.
 * 저장된 값과 다른 토큰이 들어오면(탈취/재사용) 즉시 무효화 처리할 수 있다.
 */
@Repository
public class RefreshTokenRepository {

    private static final String KEY_PREFIX = "refresh:";

    private final StringRedisTemplate redisTemplate;

    public RefreshTokenRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(Long userId, String refreshToken, long ttlSeconds) {
        redisTemplate.opsForValue().set(key(userId), refreshToken, Duration.ofSeconds(ttlSeconds));
    }

    public Optional<String> find(Long userId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key(userId)));
    }

    public void delete(Long userId) {
        redisTemplate.delete(key(userId));
    }

    private String key(Long userId) {
        return KEY_PREFIX + userId;
    }
}

package com.pantrymate.notification.devicetoken.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    Optional<DeviceToken> findByUserId(Long userId);

    @Modifying
    @Query(
            value =
                    "INSERT INTO device_tokens (user_id, fcm_token, created_at, updated_at) "
                            + "VALUES (:userId, :fcmToken, now(), now()) "
                            + "ON CONFLICT (user_id) DO UPDATE SET fcm_token = EXCLUDED.fcm_token, updated_at = now()",
            nativeQuery = true)
    void upsert(@Param("userId") Long userId, @Param("fcmToken") String fcmToken);
}

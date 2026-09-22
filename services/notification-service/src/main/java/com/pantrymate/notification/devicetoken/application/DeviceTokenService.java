package com.pantrymate.notification.devicetoken.application;

import com.pantrymate.common.exception.BusinessException;
import com.pantrymate.notification.devicetoken.domain.DeviceTokenRepository;
import com.pantrymate.notification.devicetoken.domain.exception.DeviceTokenErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;

    public DeviceTokenService(DeviceTokenRepository deviceTokenRepository) {
        this.deviceTokenRepository = deviceTokenRepository;
    }

    @Transactional
    public void register(Long userId, String fcmToken) {
        if (fcmToken == null || fcmToken.isBlank()) {
            throw new BusinessException(DeviceTokenErrorCode.DEVICE_TOKEN_INVALID);
        }

        deviceTokenRepository.upsert(userId, fcmToken);
    }
}

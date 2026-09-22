package com.pantrymate.notification.reminder.infrastructure;

import com.pantrymate.notification.reminder.domain.PushMessage;
import com.pantrymate.notification.reminder.domain.PushSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggingPushSender implements PushSender {

    @Override
    public void send(String fcmToken, PushMessage message) {
        String tokenTail = fcmToken.length() <= 6 ? fcmToken : fcmToken.substring(fcmToken.length() - 6);
        log.info("[PUSH-STUB] token=...{} message={}", tokenTail, message);
    }
}

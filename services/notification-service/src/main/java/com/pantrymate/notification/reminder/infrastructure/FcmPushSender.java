package com.pantrymate.notification.reminder.infrastructure;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.WebpushConfig;
import com.pantrymate.notification.reminder.domain.InvalidPushTokenException;
import com.pantrymate.notification.reminder.domain.PushMessage;
import com.pantrymate.notification.reminder.domain.PushSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnExpression("'${firebase.credentials-path:}' != ''")
public class FcmPushSender implements PushSender {

    private final FirebaseMessaging firebaseMessaging;
    private final String ttlSeconds;

    public FcmPushSender(
            FirebaseMessaging firebaseMessaging, @Value("${firebase.ttl-seconds:86400}") long ttlSeconds) {
        this.firebaseMessaging = firebaseMessaging;
        this.ttlSeconds = String.valueOf(ttlSeconds);
    }

    @Override
    public void send(String fcmToken, PushMessage message) {
        Message fcmMessage = Message.builder()
                .setToken(fcmToken)
                .putData("type", message.type())
                .putData("title", message.title())
                .putData("body", message.body())
                .putData("link", message.link())
                .setWebpushConfig(WebpushConfig.builder().putHeader("TTL", ttlSeconds).build())
                .build();
        try {
            firebaseMessaging.send(fcmMessage);
        } catch (FirebaseMessagingException e) {
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                throw new InvalidPushTokenException("FCM 토큰이 더 이상 유효하지 않습니다", e);
            }
            throw new IllegalStateException("FCM 발송 실패 errorCode=" + e.getMessagingErrorCode(), e);
        }
    }
}

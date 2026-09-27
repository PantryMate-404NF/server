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
            // INVALID_ARGUMENT는 잘못된 등록 토큰 외에 잘못된 데이터·TTL 등에도 쓰이지만,
            // 이 메시지의 payload/TTL은 고정값(항상 유효)이라 이 요청에서 발생하는
            // INVALID_ARGUMENT는 사실상 토큰 문제로 판단해도 안전하다.
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED
                    || e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT) {
                throw new InvalidPushTokenException("FCM 토큰이 더 이상 유효하지 않습니다", e);
            }
            throw new IllegalStateException("FCM 발송 실패 errorCode=" + e.getMessagingErrorCode(), e);
        }
    }
}

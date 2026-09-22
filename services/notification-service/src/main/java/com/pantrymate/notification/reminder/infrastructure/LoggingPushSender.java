package com.pantrymate.notification.reminder.infrastructure;

import com.pantrymate.notification.reminder.domain.PushMessage;
import com.pantrymate.notification.reminder.domain.PushSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

// prod 프로필에서는 절대 선택되지 않는다 — 키 설정 누락 시 조용히 "발송된 척" 하고
// 이력에 기록되어 그 주 리마인드가 영구 누락되는 걸 막기 위해, prod에서는 이 빈이
// 없어서 PushSender 의존성 주입 실패로 기동 자체가 실패해야 한다.
@Slf4j
@Component
@Profile("!prod")
@ConditionalOnExpression("'${firebase.credentials-path:}' == ''")
public class LoggingPushSender implements PushSender {

    @Override
    public void send(String fcmToken, PushMessage message) {
        String tokenTail = fcmToken.length() <= 6 ? fcmToken : fcmToken.substring(fcmToken.length() - 6);
        log.info("[PUSH-STUB] token=...{} message={}", tokenTail, message);
    }
}

package com.pantrymate.user.infrastructure.ai;

import com.pantrymate.user.domain.OnboardingPort;
import com.pantrymate.user.domain.OnboardingPort.OnboardingUnavailableException;
import com.pantrymate.user.domain.UserPreferenceUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** 온보딩 저장이 커밋된 뒤 AI 서버로 전달한다. AI가 실패해도 저장은 유지하고 로그만 남긴다. */
@Component
public class OnboardingRelayListener {

    private static final Logger log = LoggerFactory.getLogger(OnboardingRelayListener.class);

    private final OnboardingPort onboardingPort;
    private final boolean enabled;

    public OnboardingRelayListener(OnboardingPort onboardingPort, @Value("${ai.onboarding.enabled}") boolean enabled) {
        this.onboardingPort = onboardingPort;
        this.enabled = enabled;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void relay(UserPreferenceUpdatedEvent event) {
        if (!enabled) {
            return;
        }
        try {
            onboardingPort.saveOnboarding(event.snapshot());
        } catch (OnboardingUnavailableException e) {
            log.warn("AI 온보딩 전달 실패 userId={}: {}", event.snapshot().userId(), e.getMessage());
        }
    }
}

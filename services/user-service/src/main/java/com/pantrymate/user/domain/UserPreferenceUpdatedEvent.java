package com.pantrymate.user.domain;

/** 온보딩 설정이 커밋된 뒤 AI 서버로 전달하기 위한 이벤트. */
public record UserPreferenceUpdatedEvent(OnboardingPort.OnboardingSnapshot snapshot) {}

-- 1. device_tokens 테이블 (유저당 활성 FCM 토큰 1개, 재등록 시 upsert)
CREATE TABLE IF NOT EXISTS device_tokens (
    device_token_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    fcm_token VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. pantry_reminder_logs 테이블 (주간 팬트리 리마인드 발송 이력 — 중복 발송 방지용)
CREATE TABLE IF NOT EXISTS pantry_reminder_logs (
    pantry_reminder_log_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    sent_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS ix_pantry_reminder_logs_user_id_sent_at ON pantry_reminder_logs (user_id, sent_at DESC);

-- 1. ENUM 타입 생성
DO $$ BEGIN CREATE TYPE auth_provider AS ENUM ('KAKAO', 'NAVER'); EXCEPTION WHEN duplicate_object THEN null; END $$;
DO $$ BEGIN CREATE TYPE user_role AS ENUM ('ROLE_USER', 'ROLE_ADMIN'); EXCEPTION WHEN duplicate_object THEN null; END $$;

-- 2. users 테이블 생성
CREATE TABLE IF NOT EXISTS users (
    user_id BIGSERIAL PRIMARY KEY,
    provider auth_provider NOT NULL,
    provider_id VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    nickname VARCHAR(50) NOT NULL,
    profile_image_url VARCHAR(500),
    role user_role NOT NULL DEFAULT 'ROLE_USER',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_users_provider_provider_id UNIQUE (provider, provider_id)
);

-- 3. user_preferences 테이블 생성
CREATE TABLE IF NOT EXISTS user_preferences (
    preference_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    family_member_count INTEGER NOT NULL DEFAULT 1,
    preferred_food_types JSONB,
    allergies JSONB,
    onboarding_completed BOOLEAN NOT NULL DEFAULT FALSE,
    onboarding_step INTEGER NOT NULL DEFAULT 1,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_preferences_user_id FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE
);

-- 온보딩 마지막 단계(선호 음식 / 맛 선호도) 저장용 컬럼 추가
ALTER TABLE user_preferences
    ADD COLUMN favorite_foods JSONB,
    ADD COLUMN taste_salty INTEGER,
    ADD COLUMN taste_sweet INTEGER,
    ADD COLUMN taste_spicy INTEGER,
    ADD CONSTRAINT chk_user_preferences_taste_salty CHECK (taste_salty BETWEEN 1 AND 5),
    ADD CONSTRAINT chk_user_preferences_taste_sweet CHECK (taste_sweet BETWEEN 1 AND 5),
    ADD CONSTRAINT chk_user_preferences_taste_spicy CHECK (taste_spicy BETWEEN 1 AND 5);

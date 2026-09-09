-- 1. ENUM 타입 생성
DO $$ BEGIN CREATE TYPE storage_type AS ENUM ('REFRIGERATED', 'FROZEN', 'ROOM_TEMP'); EXCEPTION WHEN duplicate_object THEN null; END $$;
DO $$ BEGIN CREATE TYPE pantry_register_type AS ENUM ('MANUAL', 'AUTO'); EXCEPTION WHEN duplicate_object THEN null; END $$;
DO $$ BEGIN CREATE TYPE cuisine_type AS ENUM ('KOREAN', 'WESTERN', 'JAPANESE', 'CHINESE', 'ETC'); EXCEPTION WHEN duplicate_object THEN null; END $$;
DO $$ BEGIN CREATE TYPE recipe_difficulty AS ENUM ('EASY', 'NORMAL', 'HARD'); EXCEPTION WHEN duplicate_object THEN null; END $$;

-- 2. ingredients 테이블 (표준 식재료 사전 — 허브)
CREATE TABLE IF NOT EXISTS ingredients (
    ingredient_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    category VARCHAR(30),
    image_url VARCHAR(500),
    default_storage_type storage_type,
    default_expiry_days INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. pantry_items 테이블
CREATE TABLE IF NOT EXISTS pantry_items (
    pantry_item_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    ingredient_id BIGINT REFERENCES ingredients(ingredient_id),
    name VARCHAR(50) NOT NULL,
    image_url VARCHAR(500),
    storage_type storage_type NOT NULL,
    expiry_date DATE,
    is_cookable BOOLEAN NOT NULL DEFAULT TRUE,
    register_type pantry_register_type NOT NULL DEFAULT 'MANUAL',
    order_item_id BIGINT UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS ix_pantry_items_user_id_expiry ON pantry_items (user_id, expiry_date);

-- 4. recipes 테이블
CREATE TABLE IF NOT EXISTS recipes (
    recipe_id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    cuisine_type cuisine_type NOT NULL,
    cooking_time INTEGER NOT NULL,
    servings INTEGER NOT NULL DEFAULT 1,
    difficulty recipe_difficulty NOT NULL,
    thumbnail_url VARCHAR(500),
    is_published BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 5. recipe_ingredients 테이블
CREATE TABLE IF NOT EXISTS recipe_ingredients (
    recipe_ingredient_id BIGSERIAL PRIMARY KEY,
    recipe_id BIGINT NOT NULL REFERENCES recipes(recipe_id) ON DELETE CASCADE,
    ingredient_id BIGINT NOT NULL REFERENCES ingredients(ingredient_id),
    name VARCHAR(50) NOT NULL,
    is_main BOOLEAN NOT NULL DEFAULT FALSE,
    required_amount NUMERIC(10,2),
    unit VARCHAR(20)
);
CREATE INDEX IF NOT EXISTS ix_recipe_ingredients_recipe_id ON recipe_ingredients (recipe_id);

-- 6. recipe_steps 테이블
CREATE TABLE IF NOT EXISTS recipe_steps (
    recipe_step_id BIGSERIAL PRIMARY KEY,
    recipe_id BIGINT NOT NULL REFERENCES recipes(recipe_id) ON DELETE CASCADE,
    step_number INTEGER NOT NULL,
    description TEXT NOT NULL,
    image_url VARCHAR(500),
    CONSTRAINT uk_recipe_steps_recipe_step UNIQUE (recipe_id, step_number)
);
CREATE INDEX IF NOT EXISTS ix_recipe_steps_recipe_id ON recipe_steps (recipe_id);

-- 7. cooking_histories 테이블
CREATE TABLE IF NOT EXISTS cooking_histories (
    history_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    recipe_id BIGINT NOT NULL REFERENCES recipes(recipe_id) ON DELETE CASCADE,
    cooked_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS ix_cooking_histories_user_id ON cooking_histories (user_id);

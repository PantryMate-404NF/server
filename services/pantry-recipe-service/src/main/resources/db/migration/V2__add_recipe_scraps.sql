CREATE TABLE IF NOT EXISTS recipe_scraps (
    recipe_scrap_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    recipe_id BIGINT NOT NULL REFERENCES recipes(recipe_id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_recipe_scraps_user_recipe UNIQUE (user_id, recipe_id)
);
CREATE INDEX IF NOT EXISTS ix_recipe_scraps_user_id ON recipe_scraps (user_id, created_at DESC);

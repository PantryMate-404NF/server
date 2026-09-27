CREATE INDEX IF NOT EXISTS idx_recipes_title_fts ON recipes USING GIN (to_tsvector('simple', title));
CREATE INDEX IF NOT EXISTS idx_recipe_ingredients_name_fts ON recipe_ingredients USING GIN (to_tsvector('simple', name));
CREATE INDEX IF NOT EXISTS idx_pantry_items_name_fts ON pantry_items USING GIN (to_tsvector('simple', name));

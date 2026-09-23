package com.pantrymate.pantryrecipe.recipe.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    Page<Recipe> findByPublishedTrueOrderByRecipeIdAsc(Pageable pageable);

    Optional<Recipe> findByRecipeIdAndPublishedTrue(Long recipeId);

    @Query(
            value =
                    """
            WITH match_counts AS (
                SELECT recipe_id, COUNT(*) AS match_count
                FROM recipe_ingredients
                WHERE ingredient_id IN (:ingredientIds)
                GROUP BY recipe_id
            )
            SELECT r.recipe_id
            FROM recipes r
            LEFT JOIN match_counts m ON m.recipe_id = r.recipe_id
            WHERE r.is_published = true
            ORDER BY COALESCE(m.match_count, 0) DESC, r.recipe_id ASC
            """,
            countQuery = "SELECT COUNT(*) FROM recipes WHERE is_published = true",
            nativeQuery = true)
    Page<Long> findRecipeIdsRankedByIngredientMatch(
            @Param("ingredientIds") List<Long> ingredientIds, Pageable pageable);

    @Query(
            value =
                    """
            SELECT r.recipe_id
            FROM recipes r
            WHERE r.is_published = true
              AND (
                to_tsvector('simple', r.title) @@ plainto_tsquery('simple', :keyword)
                OR EXISTS (
                    SELECT 1 FROM recipe_ingredients ri
                    WHERE ri.recipe_id = r.recipe_id
                      AND to_tsvector('simple', ri.name) @@ plainto_tsquery('simple', :keyword)
                )
              )
            ORDER BY
              CASE WHEN to_tsvector('simple', r.title) @@ plainto_tsquery('simple', :keyword) THEN 0 ELSE 1 END,
              r.recipe_id
            """,
            countQuery =
                    """
            SELECT COUNT(*)
            FROM recipes r
            WHERE r.is_published = true
              AND (
                to_tsvector('simple', r.title) @@ plainto_tsquery('simple', :keyword)
                OR EXISTS (
                    SELECT 1 FROM recipe_ingredients ri
                    WHERE ri.recipe_id = r.recipe_id
                      AND to_tsvector('simple', ri.name) @@ plainto_tsquery('simple', :keyword)
                )
              )
            """,
            nativeQuery = true)
    Page<Long> searchRecipeIds(@Param("keyword") String keyword, Pageable pageable);
}

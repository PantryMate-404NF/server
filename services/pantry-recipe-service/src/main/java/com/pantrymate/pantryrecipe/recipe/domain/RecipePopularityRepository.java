package com.pantrymate.pantryrecipe.recipe.domain;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/** AI 추천을 쓸 수 없을 때 대신 보여줄 인기순(스크랩 수) 조회. 재료 알레르기 사전으로 알레르기 재료가 든 레시피는 제외한다. */
public interface RecipePopularityRepository extends Repository<Recipe, Long> {

    @Query(
            value =
                    """
            SELECT r.recipe_id
            FROM recipes r
            LEFT JOIN (
                SELECT recipe_id, COUNT(*) AS scrap_count FROM recipe_scraps GROUP BY recipe_id
            ) s ON s.recipe_id = r.recipe_id
            WHERE r.is_published = true
              AND (CAST(:maxMinutes AS integer) IS NULL OR r.cooking_time <= CAST(:maxMinutes AS integer))
              AND (:allergyCount = 0 OR NOT EXISTS (
                    SELECT 1
                    FROM recipe_ingredients ri
                    JOIN ingredients i ON i.ingredient_id = ri.ingredient_id
                    WHERE ri.recipe_id = r.recipe_id
                      AND i.allergens && string_to_array(:allergyCsv, '|')))
            ORDER BY COALESCE(s.scrap_count, 0) DESC, r.recipe_id ASC
            LIMIT :limit
            """,
            nativeQuery = true)
    List<Long> findPopularRecipeIds(
            @Param("allergyCount") int allergyCount,
            @Param("allergyCsv") String allergyCsv,
            @Param("maxMinutes") Integer maxMinutes,
            @Param("limit") int limit);
}

package com.pantrymate.pantryrecipe.recipe.domain;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecipeScrapRepository extends JpaRepository<RecipeScrap, Long> {

    Optional<RecipeScrap> findByUserIdAndRecipe_RecipeId(Long userId, Long recipeId);

    List<RecipeScrap> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Modifying
    @Query(
            value =
                    "INSERT INTO recipe_scraps (user_id, recipe_id, created_at) "
                            + "VALUES (:userId, :recipeId, CURRENT_TIMESTAMP) "
                            + "ON CONFLICT (user_id, recipe_id) DO NOTHING",
            nativeQuery = true)
    int insertIfAbsent(@Param("userId") Long userId, @Param("recipeId") Long recipeId);

    @Query(
            "SELECT new com.pantrymate.pantryrecipe.recipe.domain.RecipeScrapCount(s.recipe.recipeId, COUNT(s)) "
                    + "FROM RecipeScrap s WHERE s.recipe.recipeId IN :recipeIds GROUP BY s.recipe.recipeId")
    List<RecipeScrapCount> countByRecipeIds(@Param("recipeIds") Collection<Long> recipeIds);
}

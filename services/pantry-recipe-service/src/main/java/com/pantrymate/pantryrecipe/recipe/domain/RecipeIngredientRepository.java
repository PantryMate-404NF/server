package com.pantrymate.pantryrecipe.recipe.domain;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Long> {

    List<RecipeIngredient> findByRecipe_RecipeIdOrderByRecipeIngredientIdAsc(Long recipeId);

    @Query(
            """
            SELECT new com.pantrymate.pantryrecipe.recipe.domain.RecipeIngredientRow(
                ri.recipe.recipeId, ri.ingredient.ingredientId, ri.name, ri.main, ri.unit)
            FROM RecipeIngredient ri
            WHERE ri.recipe.recipeId IN :recipeIds
            ORDER BY ri.recipe.recipeId ASC, ri.recipeIngredientId ASC
            """)
    List<RecipeIngredientRow> findRowsByRecipeIds(@Param("recipeIds") Collection<Long> recipeIds);
}

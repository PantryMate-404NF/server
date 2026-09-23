package com.pantrymate.pantryrecipe.recipe.domain;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Long> {

    List<RecipeIngredient> findByRecipe_RecipeIdOrderByRecipeIngredientIdAsc(Long recipeId);

    List<RecipeIngredient> findByRecipe_RecipeIdIn(Collection<Long> recipeIds);
}

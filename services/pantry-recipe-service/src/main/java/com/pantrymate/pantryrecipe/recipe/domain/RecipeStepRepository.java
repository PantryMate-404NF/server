package com.pantrymate.pantryrecipe.recipe.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeStepRepository extends JpaRepository<RecipeStep, Long> {

    List<RecipeStep> findByRecipe_RecipeIdOrderByStepNumberAsc(Long recipeId);
}

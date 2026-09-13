package com.pantrymate.pantryrecipe.recipe.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    List<Recipe> findByPublishedTrueOrderByRecipeIdAsc();

    Optional<Recipe> findByRecipeIdAndPublishedTrue(Long recipeId);
}

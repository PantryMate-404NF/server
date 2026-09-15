package com.pantrymate.pantryrecipe.recipe.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeScrapRepository extends JpaRepository<RecipeScrap, Long> {

    Optional<RecipeScrap> findByUserIdAndRecipe_RecipeId(Long userId, Long recipeId);

    List<RecipeScrap> findByUserIdOrderByCreatedAtDesc(Long userId);
}

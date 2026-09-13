package com.pantrymate.pantryrecipe.recipe.application;

import com.pantrymate.pantryrecipe.recipe.domain.Recipe;
import com.pantrymate.pantryrecipe.recipe.domain.RecipeRepository;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.RecipeResponseDto;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;

    public RecipeService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    @Transactional(readOnly = true)
    public List<RecipeResponseDto> getAll() {
        List<Recipe> recipes = recipeRepository.findByPublishedTrueOrderByRecipeIdAsc();
        return recipes.stream().map(RecipeResponseDto::from).toList();
    }
}

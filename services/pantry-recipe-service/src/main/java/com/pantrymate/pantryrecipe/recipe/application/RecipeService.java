package com.pantrymate.pantryrecipe.recipe.application;

import com.pantrymate.common.exception.BusinessException;
import com.pantrymate.pantryrecipe.recipe.domain.Recipe;
import com.pantrymate.pantryrecipe.recipe.domain.RecipeIngredientRepository;
import com.pantrymate.pantryrecipe.recipe.domain.RecipeRepository;
import com.pantrymate.pantryrecipe.recipe.domain.RecipeStepRepository;
import com.pantrymate.pantryrecipe.recipe.domain.exception.RecipeErrorCode;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.RecipeDetailResponseDto;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.RecipeIngredientResponseDto;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.RecipeResponseDto;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.RecipeStepResponseDto;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeStepRepository recipeStepRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;

    public RecipeService(
            RecipeRepository recipeRepository,
            RecipeStepRepository recipeStepRepository,
            RecipeIngredientRepository recipeIngredientRepository) {
        this.recipeRepository = recipeRepository;
        this.recipeStepRepository = recipeStepRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
    }

    @Transactional(readOnly = true)
    public List<RecipeResponseDto> getAll() {
        List<Recipe> recipes = recipeRepository.findByPublishedTrueOrderByRecipeIdAsc();
        return recipes.stream().map(RecipeResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public RecipeDetailResponseDto getById(Long recipeId) {
        Recipe recipe = recipeRepository
                .findByRecipeIdAndPublishedTrue(recipeId)
                .orElseThrow(() -> new BusinessException(RecipeErrorCode.RECIPE_NOTFOUND_ID));

        List<RecipeStepResponseDto> steps = recipeStepRepository
                .findByRecipe_RecipeIdOrderByStepNumberAsc(recipeId)
                .stream()
                .map(RecipeStepResponseDto::from)
                .toList();

        List<RecipeIngredientResponseDto> ingredients = recipeIngredientRepository
                .findByRecipe_RecipeIdOrderByRecipeIngredientIdAsc(recipeId)
                .stream()
                .map(RecipeIngredientResponseDto::from)
                .toList();

        return RecipeDetailResponseDto.of(recipe, steps, ingredients);
    }
}

package com.pantrymate.pantryrecipe.recipe.application;

import com.pantrymate.common.exception.BusinessException;
import com.pantrymate.pantryrecipe.pantry.domain.PantryItem;
import com.pantrymate.pantryrecipe.pantry.domain.PantryItemRepository;
import com.pantrymate.pantryrecipe.recipe.domain.Recipe;
import com.pantrymate.pantryrecipe.recipe.domain.RecipeIngredient;
import com.pantrymate.pantryrecipe.recipe.domain.RecipeIngredientRepository;
import com.pantrymate.pantryrecipe.recipe.domain.RecipeRepository;
import com.pantrymate.pantryrecipe.recipe.domain.RecipeStepRepository;
import com.pantrymate.pantryrecipe.recipe.domain.exception.RecipeErrorCode;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.RecipeDetailResponseDto;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.RecipeIngredientPantryMatchResponseDto;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.RecipeIngredientPantryMatchResponseDto.MatchedPantryItemResponseDto;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.RecipeIngredientResponseDto;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.RecipePantryMatchResponseDto;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.RecipeResponseDto;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.RecipeStepResponseDto;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeStepRepository recipeStepRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final PantryItemRepository pantryItemRepository;

    public RecipeService(
            RecipeRepository recipeRepository,
            RecipeStepRepository recipeStepRepository,
            RecipeIngredientRepository recipeIngredientRepository,
            PantryItemRepository pantryItemRepository) {
        this.recipeRepository = recipeRepository;
        this.recipeStepRepository = recipeStepRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.pantryItemRepository = pantryItemRepository;
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

    @Transactional(readOnly = true)
    public RecipePantryMatchResponseDto getPantryMatch(Long userId, Long recipeId) {
        recipeRepository
                .findByRecipeIdAndPublishedTrue(recipeId)
                .orElseThrow(() -> new BusinessException(RecipeErrorCode.RECIPE_NOTFOUND_ID));

        List<RecipeIngredient> recipeIngredients =
                recipeIngredientRepository.findByRecipe_RecipeIdOrderByRecipeIngredientIdAsc(recipeId);

        List<Long> ingredientIds =
                recipeIngredients.stream().map(ri -> ri.getIngredient().getIngredientId()).toList();
        Map<Long, List<PantryItem>> pantryItemsByIngredientId =
                pantryItemRepository.findByUserIdAndIngredient_IngredientIdIn(userId, ingredientIds).stream()
                        .collect(Collectors.groupingBy(item -> item.getIngredient().getIngredientId()));

        List<RecipeIngredientPantryMatchResponseDto> ingredients = recipeIngredients.stream()
                .map(ri -> {
                    List<PantryItem> matched =
                            pantryItemsByIngredientId.getOrDefault(ri.getIngredient().getIngredientId(), List.of());
                    List<MatchedPantryItemResponseDto> matchedDtos =
                            matched.stream().map(MatchedPantryItemResponseDto::from).toList();
                    return new RecipeIngredientPantryMatchResponseDto(
                            ri.getIngredient().getIngredientId(), ri.getName(), !matchedDtos.isEmpty(), matchedDtos);
                })
                .toList();

        return new RecipePantryMatchResponseDto(recipeId, ingredients);
    }
}

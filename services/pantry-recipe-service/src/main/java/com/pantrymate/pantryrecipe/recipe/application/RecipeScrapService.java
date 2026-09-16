package com.pantrymate.pantryrecipe.recipe.application;

import com.pantrymate.common.exception.BusinessException;
import com.pantrymate.pantryrecipe.recipe.domain.Recipe;
import com.pantrymate.pantryrecipe.recipe.domain.RecipeRepository;
import com.pantrymate.pantryrecipe.recipe.domain.RecipeScrap;
import com.pantrymate.pantryrecipe.recipe.domain.RecipeScrapRepository;
import com.pantrymate.pantryrecipe.recipe.domain.exception.RecipeErrorCode;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.RecipeResponseDto;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecipeScrapService {

    private final RecipeRepository recipeRepository;
    private final RecipeScrapRepository recipeScrapRepository;

    public RecipeScrapService(RecipeRepository recipeRepository, RecipeScrapRepository recipeScrapRepository) {
        this.recipeRepository = recipeRepository;
        this.recipeScrapRepository = recipeScrapRepository;
    }

    @Transactional
    public void scrap(Long userId, Long recipeId) {
        Recipe recipe = recipeRepository
                .findByRecipeIdAndPublishedTrue(recipeId)
                .orElseThrow(() -> new BusinessException(RecipeErrorCode.RECIPE_NOTFOUND_ID));
        recipeScrapRepository.insertIfAbsent(userId, recipe.getRecipeId());
    }

    @Transactional
    public void unscrap(Long userId, Long recipeId) {
        recipeScrapRepository.findByUserIdAndRecipe_RecipeId(userId, recipeId).ifPresent(recipeScrapRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<RecipeResponseDto> getScraps(Long userId) {
        return recipeScrapRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(scrap -> RecipeResponseDto.from(scrap.getRecipe()))
                .toList();
    }
}

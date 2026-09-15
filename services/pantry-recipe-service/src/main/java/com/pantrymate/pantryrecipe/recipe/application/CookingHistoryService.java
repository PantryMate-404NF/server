package com.pantrymate.pantryrecipe.recipe.application;

import com.pantrymate.common.exception.BusinessException;
import com.pantrymate.pantryrecipe.recipe.domain.CookingHistory;
import com.pantrymate.pantryrecipe.recipe.domain.CookingHistoryRepository;
import com.pantrymate.pantryrecipe.recipe.domain.Recipe;
import com.pantrymate.pantryrecipe.recipe.domain.RecipeRepository;
import com.pantrymate.pantryrecipe.recipe.domain.exception.RecipeErrorCode;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.CookingHistoryResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CookingHistoryService {

    private final RecipeRepository recipeRepository;
    private final CookingHistoryRepository cookingHistoryRepository;

    public CookingHistoryService(RecipeRepository recipeRepository, CookingHistoryRepository cookingHistoryRepository) {
        this.recipeRepository = recipeRepository;
        this.cookingHistoryRepository = cookingHistoryRepository;
    }

    @Transactional
    public CookingHistoryResponseDto complete(Long userId, Long recipeId) {
        Recipe recipe = recipeRepository
                .findByRecipeIdAndPublishedTrue(recipeId)
                .orElseThrow(() -> new BusinessException(RecipeErrorCode.RECIPE_NOTFOUND_ID));

        CookingHistory history = cookingHistoryRepository.save(CookingHistory.create(userId, recipe));
        return CookingHistoryResponseDto.from(history);
    }
}

package com.pantrymate.pantryrecipe.recipe.application;

import com.pantrymate.common.exception.BusinessException;
import com.pantrymate.pantryrecipe.pantry.application.PantryItemService;
import com.pantrymate.pantryrecipe.recipe.domain.CookingHistory;
import com.pantrymate.pantryrecipe.recipe.domain.CookingHistoryRepository;
import com.pantrymate.pantryrecipe.recipe.domain.Recipe;
import com.pantrymate.pantryrecipe.recipe.domain.RecipeRepository;
import com.pantrymate.pantryrecipe.recipe.domain.exception.RecipeErrorCode;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.CookingHistoryResponseDto;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CookingHistoryService {

    private final RecipeRepository recipeRepository;
    private final CookingHistoryRepository cookingHistoryRepository;
    private final PantryItemService pantryItemService;

    public CookingHistoryService(
            RecipeRepository recipeRepository,
            CookingHistoryRepository cookingHistoryRepository,
            PantryItemService pantryItemService) {
        this.recipeRepository = recipeRepository;
        this.cookingHistoryRepository = cookingHistoryRepository;
        this.pantryItemService = pantryItemService;
    }

    @Transactional
    public CookingHistoryResponseDto complete(Long userId, Long recipeId, List<Long> pantryItemIds) {
        Recipe recipe = recipeRepository
                .findByRecipeIdAndPublishedTrue(recipeId)
                .orElseThrow(() -> new BusinessException(RecipeErrorCode.RECIPE_NOTFOUND_ID));

        CookingHistory history = cookingHistoryRepository.save(CookingHistory.create(userId, recipe));

        if (pantryItemIds != null && !pantryItemIds.isEmpty()) {
            pantryItemService.deleteAllByUser(userId, pantryItemIds);
        }

        return CookingHistoryResponseDto.from(history);
    }
}

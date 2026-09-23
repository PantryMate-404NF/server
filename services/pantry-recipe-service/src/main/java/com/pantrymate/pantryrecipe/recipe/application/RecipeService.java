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
import com.pantrymate.pantryrecipe.recipe.presentation.dto.RecipeFilterIngredientResponseDto;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.RecipeIngredientPantryMatchResponseDto;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.RecipeIngredientPantryMatchResponseDto.MatchedPantryItemResponseDto;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.RecipeIngredientResponseDto;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.RecipeListResponseDto;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.RecipePantryMatchResponseDto;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.RecipeResponseDto;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.RecipeStepResponseDto;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

    private static final int MAX_FILTER_INGREDIENTS = 3;

    @Transactional(readOnly = true)
    public RecipeListResponseDto getAll(List<Long> ingredientIds, Pageable pageable) {
        if (ingredientIds == null || ingredientIds.isEmpty()) {
            Page<RecipeResponseDto> page =
                    recipeRepository.findByPublishedTrueOrderByRecipeIdAsc(pageable).map(RecipeResponseDto::from);
            return RecipeListResponseDto.from(page);
        }
        if (ingredientIds.size() > MAX_FILTER_INGREDIENTS) {
            throw new BusinessException(RecipeErrorCode.RECIPE_INVALID_FILTER);
        }

        Page<Long> idPage = recipeRepository.findRecipeIdsRankedByIngredientMatch(ingredientIds, pageable);
        return RecipeListResponseDto.from(toOrderedRecipePage(idPage));
    }

    @Transactional(readOnly = true)
    public List<RecipeFilterIngredientResponseDto> getFilterIngredients(Long userId) {
        List<PantryItem> pantryItems = pantryItemRepository.findByUserIdAndIngredientIsNotNull(userId);

        Map<Long, PantryItem> representativeByIngredientId = pantryItems.stream()
                .collect(Collectors.toMap(
                        item -> item.getIngredient().getIngredientId(),
                        item -> item,
                        (a, b) -> {
                            int compared = a.getExpiryDate().compareTo(b.getExpiryDate());
                            if (compared != 0) {
                                return compared < 0 ? a : b;
                            }
                            return a.getCreatedAt().isBefore(b.getCreatedAt()) ? a : b;
                        }));

        List<PantryItem> ordered = representativeByIngredientId.values().stream()
                .sorted(Comparator.comparing(PantryItem::getExpiryDate).thenComparing(PantryItem::getCreatedAt))
                .toList();

        List<RecipeFilterIngredientResponseDto> result = new ArrayList<>();
        int defaultSelectedCount = 0;
        for (PantryItem item : ordered) {
            boolean expired = item.getExpiryDate().isBefore(LocalDate.now());
            boolean defaultSelected = !expired && defaultSelectedCount < MAX_FILTER_INGREDIENTS;
            if (defaultSelected) {
                defaultSelectedCount++;
            }
            result.add(RecipeFilterIngredientResponseDto.of(item, defaultSelected));
        }
        return result;
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

    @Transactional(readOnly = true)
    public RecipeListResponseDto search(String keyword, Pageable pageable) {
        String trimmed = keyword == null ? "" : keyword.trim();
        if (trimmed.isEmpty()) {
            throw new BusinessException(RecipeErrorCode.RECIPE_INVALID_SEARCH_KEYWORD);
        }

        Page<Long> idPage = recipeRepository.searchRecipeIds(trimmed, pageable);
        return RecipeListResponseDto.from(toOrderedRecipePage(idPage));
    }

    private Page<RecipeResponseDto> toOrderedRecipePage(Page<Long> idPage) {
        Map<Long, Recipe> recipeById = recipeRepository.findAllById(idPage.getContent()).stream()
                .collect(Collectors.toMap(Recipe::getRecipeId, recipe -> recipe));
        List<RecipeResponseDto> content = idPage.getContent().stream()
                .map(recipeById::get)
                .filter(Objects::nonNull)
                .map(RecipeResponseDto::from)
                .toList();
        return new PageImpl<>(content, idPage.getPageable(), idPage.getTotalElements());
    }
}

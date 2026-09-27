package com.pantrymate.pantryrecipe.recipe.application;

import com.pantrymate.common.exception.BusinessException;
import com.pantrymate.pantryrecipe.ingredient.domain.Ingredient;
import com.pantrymate.pantryrecipe.ingredient.domain.IngredientRepository;
import com.pantrymate.pantryrecipe.pantry.domain.PantryItem;
import com.pantrymate.pantryrecipe.pantry.domain.PantryItemRepository;
import com.pantrymate.pantryrecipe.recipe.domain.Recipe;
import com.pantrymate.pantryrecipe.recipe.domain.RecipePopularityRepository;
import com.pantrymate.pantryrecipe.recipe.domain.RecipeRepository;
import com.pantrymate.pantryrecipe.recipe.domain.RecommendationPort;
import com.pantrymate.pantryrecipe.recipe.domain.RecommendationPort.PantryEntry;
import com.pantrymate.pantryrecipe.recipe.domain.RecommendationPort.Recommendation;
import com.pantrymate.pantryrecipe.recipe.domain.RecommendationPort.RecommendationQuery;
import com.pantrymate.pantryrecipe.recipe.domain.RecommendationPort.RecommendationUnavailableException;
import com.pantrymate.pantryrecipe.recipe.domain.RecommendationPort.RecommendedItem;
import com.pantrymate.pantryrecipe.recipe.domain.UserAllergyPort;
import com.pantrymate.pantryrecipe.recipe.domain.UserAllergyPort.UserAllergyUnavailableException;
import com.pantrymate.pantryrecipe.recipe.domain.exception.RecipeErrorCode;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.RecipeRecommendResponseDto;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.RecipeRecommendResponseDto.Item;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.RecipeRecommendResponseDto.MissingIngredient;
import com.pantrymate.pantryrecipe.recipe.presentation.dto.RecipeResponseDto;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RecipeRecommendService {

    private static final Logger log = LoggerFactory.getLogger(RecipeRecommendService.class);
    private static final String SOURCE_AI = "AI";
    private static final String SOURCE_POPULARITY = "POPULARITY";
    private static final String ALLERGY_DELIMITER = "|";

    private final RecommendationPort recommendationPort;
    private final UserAllergyPort userAllergyPort;
    private final PantryItemRepository pantryItemRepository;
    private final RecipeRepository recipeRepository;
    private final RecipePopularityRepository recipePopularityRepository;
    private final IngredientRepository ingredientRepository;

    public RecipeRecommendService(
            RecommendationPort recommendationPort,
            UserAllergyPort userAllergyPort,
            PantryItemRepository pantryItemRepository,
            RecipeRepository recipeRepository,
            RecipePopularityRepository recipePopularityRepository,
            IngredientRepository ingredientRepository) {
        this.recommendationPort = recommendationPort;
        this.userAllergyPort = userAllergyPort;
        this.pantryItemRepository = pantryItemRepository;
        this.recipeRepository = recipeRepository;
        this.recipePopularityRepository = recipePopularityRepository;
        this.ingredientRepository = ingredientRepository;
    }

    /** AI 서버 호출(수 초)이 DB 트랜잭션을 붙잡지 않도록 이 메서드는 트랜잭션 없이 각 조회를 짧게 수행한다. */
    public RecipeRecommendResponseDto recommend(Long userId, int size, Integer maxMinutes) {
        List<String> allergies = getAllergies(userId);
        List<PantryEntry> pantry = buildPantry(userId);

        RecommendationQuery query = new RecommendationQuery(
                userId,
                "c-" + userId + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12),
                size,
                maxMinutes,
                pantry,
                allergies);
        try {
            Recommendation recommendation = recommendationPort.recommend(query);
            List<Item> items = toItems(recommendation.items());
            if (!items.isEmpty()) {
                return new RecipeRecommendResponseDto(recommendation.requestId(), SOURCE_AI, items);
            }
            log.warn("AI 추천 결과에 표시 가능한 레시피가 없어 인기순으로 대체한다 userId={}", userId);
        } catch (RecommendationUnavailableException e) {
            log.warn("AI 추천을 사용할 수 없어 인기순으로 대체한다 userId={}: {}", userId, e.getMessage());
        }
        return fallback(allergies, size, maxMinutes);
    }

    private List<String> getAllergies(Long userId) {
        try {
            return userAllergyPort.getAllergies(userId);
        } catch (UserAllergyUnavailableException e) {
            // 알레르기를 모르는 채로 추천하면 알레르기 재료가 섞일 수 있어 추천 자체를 제공하지 않는다.
            log.error("알레르기 조회 실패로 추천을 제공하지 않는다 userId={}: {}", userId, e.getMessage());
            throw new BusinessException(RecipeErrorCode.RECIPE_UNAVAILABLE_RECOMMEND);
        }
    }

    /** 사용자가 직접 등록한 요리가능 재료만 전달한다(상비 재료는 AI가 자동으로 더한다). 같은 재료는 소비기한이 빠른 것 하나만 보낸다. */
    private List<PantryEntry> buildPantry(Long userId) {
        Map<Long, PantryItem> earliestByIngredient = new LinkedHashMap<>();
        for (PantryItem item : pantryItemRepository.findCookableNonStapleByUserId(userId)) {
            earliestByIngredient.merge(
                    item.getIngredient().getIngredientId(), item, (a, b) -> b.getExpiryDate().isBefore(a.getExpiryDate()) ? b : a);
        }
        return earliestByIngredient.values().stream()
                .map(item -> new PantryEntry(
                        item.getIngredient().getIngredientId(),
                        item.getPurchaseDate() != null
                                ? item.getPurchaseDate()
                                : item.getCreatedAt().toLocalDate(),
                        item.getExpiryDate()))
                .toList();
    }

    private List<Item> toItems(List<RecommendedItem> recommended) {
        Map<Long, Recipe> recipeById = recipeRepository
                .findAllById(recommended.stream().map(RecommendedItem::recipeId).toList())
                .stream()
                .filter(Recipe::isPublished)
                .collect(Collectors.toMap(Recipe::getRecipeId, recipe -> recipe));
        Map<Long, String> ingredientNames = ingredientNames(recommended);

        List<RecommendedItem> ordered = recommended.stream()
                .sorted(Comparator.comparingInt(RecommendedItem::finalRank))
                .toList();
        List<Item> items = new ArrayList<>();
        for (RecommendedItem item : ordered) {
            Recipe recipe = recipeById.get(item.recipeId());
            if (recipe == null) {
                continue;
            }
            List<MissingIngredient> missing = item.missingIds().stream()
                    .map(id -> new MissingIngredient(id, ingredientNames.get(id)))
                    .filter(m -> m.name() != null)
                    .toList();
            items.add(new Item(
                    items.size() + 1,
                    item.reason(),
                    item.coverage(),
                    item.missingCount(),
                    missing,
                    RecipeResponseDto.from(recipe)));
        }
        return items;
    }

    private Map<Long, String> ingredientNames(List<RecommendedItem> recommended) {
        List<Long> ids = recommended.stream()
                .flatMap(item -> item.missingIds().stream())
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return ingredientRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Ingredient::getIngredientId, Ingredient::getName));
    }

    private RecipeRecommendResponseDto fallback(List<String> allergies, int size, Integer maxMinutes) {
        List<Long> ids = recipePopularityRepository.findPopularRecipeIds(
                allergies.size(), String.join(ALLERGY_DELIMITER, allergies), maxMinutes, size);
        Map<Long, Recipe> recipeById =
                recipeRepository.findAllById(ids).stream().collect(Collectors.toMap(Recipe::getRecipeId, r -> r));
        List<Item> items = new ArrayList<>();
        for (Long id : ids) {
            Recipe recipe = recipeById.get(id);
            if (recipe != null) {
                items.add(new Item(items.size() + 1, null, null, null, List.of(), RecipeResponseDto.from(recipe)));
            }
        }
        return new RecipeRecommendResponseDto(null, SOURCE_POPULARITY, items);
    }
}

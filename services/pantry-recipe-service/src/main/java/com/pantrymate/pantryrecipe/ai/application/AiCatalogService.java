package com.pantrymate.pantryrecipe.ai.application;

import com.pantrymate.common.exception.BusinessException;
import com.pantrymate.common.exception.CommonErrorCode;
import com.pantrymate.pantryrecipe.ai.presentation.dto.AiIngredientListResponseDto;
import com.pantrymate.pantryrecipe.ai.presentation.dto.AiRecipeListResponseDto;
import com.pantrymate.pantryrecipe.ai.presentation.dto.AiRecipeListResponseDto.IngredientItem;
import com.pantrymate.pantryrecipe.ai.presentation.dto.AiRecipeListResponseDto.Popularity;
import com.pantrymate.pantryrecipe.ai.presentation.dto.AiRecipeListResponseDto.Rating;
import com.pantrymate.pantryrecipe.ingredient.domain.IngredientRepository;
import com.pantrymate.pantryrecipe.recipe.domain.Recipe;
import com.pantrymate.pantryrecipe.recipe.domain.RecipeIngredientRepository;
import com.pantrymate.pantryrecipe.recipe.domain.RecipeIngredientRow;
import com.pantrymate.pantryrecipe.recipe.domain.RecipeRepository;
import com.pantrymate.pantryrecipe.recipe.domain.RecipeScrapCount;
import com.pantrymate.pantryrecipe.recipe.domain.RecipeScrapRepository;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiCatalogService {

    public static final int DEFAULT_LIMIT = 1000;
    public static final int MAX_LIMIT = 5000;
    private static final Pattern CURSOR_ID = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");

    private final IngredientRepository ingredientRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final RecipeScrapRepository recipeScrapRepository;
    private final boolean includeCuratedIngredientFields;

    public AiCatalogService(
            IngredientRepository ingredientRepository,
            RecipeRepository recipeRepository,
            RecipeIngredientRepository recipeIngredientRepository,
            RecipeScrapRepository recipeScrapRepository,
            @Value("${ai.sync.include-curated-ingredient-fields}") boolean includeCuratedIngredientFields) {
        this.ingredientRepository = ingredientRepository;
        this.recipeRepository = recipeRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.recipeScrapRepository = recipeScrapRepository;
        this.includeCuratedIngredientFields = includeCuratedIngredientFields;
    }

    @Transactional(readOnly = true)
    public AiIngredientListResponseDto getIngredients() {
        return new AiIngredientListResponseDto(ingredientRepository.findAllByOrderByIngredientIdAsc().stream()
                .map(ingredient -> AiIngredientListResponseDto.Item.of(ingredient, includeCuratedIngredientFields))
                .toList());
    }

    @Transactional(readOnly = true)
    public AiRecipeListResponseDto getRecipes(OffsetDateTime updatedAfter, String cursor, Integer requestedLimit) {
        int limit = resolveLimit(requestedLimit);
        long afterId = decodeCursor(cursor);
        PageRequest pageRequest = PageRequest.of(0, limit + 1);

        List<Recipe> fetched = updatedAfter == null
                ? recipeRepository.findByRecipeIdGreaterThanOrderByRecipeIdAsc(afterId, pageRequest)
                : recipeRepository.findByRecipeIdGreaterThanAndUpdatedAtAfterOrderByRecipeIdAsc(
                        afterId, updatedAfter, pageRequest);
        boolean hasNext = fetched.size() > limit;
        List<Recipe> recipes = hasNext ? fetched.subList(0, limit) : fetched;

        List<Long> recipeIds = recipes.stream().map(Recipe::getRecipeId).toList();
        Map<Long, List<IngredientItem>> ingredientsByRecipeId = recipeIds.isEmpty()
                ? Map.of()
                : recipeIngredientRepository.findRowsByRecipeIds(recipeIds).stream()
                        .collect(Collectors.groupingBy(
                                RecipeIngredientRow::recipeId,
                                Collectors.mapping(
                                        row -> new IngredientItem(
                                                row.ingredientId(), row.name(), row.main(), row.unit()),
                                        Collectors.toList())));
        Map<Long, Long> scrapCountByRecipeId = recipeIds.isEmpty()
                ? Map.of()
                : recipeScrapRepository.countByRecipeIds(recipeIds).stream()
                        .collect(Collectors.toMap(RecipeScrapCount::recipeId, RecipeScrapCount::count));

        List<AiRecipeListResponseDto.Item> items = recipes.stream()
                .map(recipe -> new AiRecipeListResponseDto.Item(
                        recipe.getRecipeId(),
                        recipe.getTitle(),
                        recipe.getCuisineType().name(),
                        recipe.getCookingTime(),
                        recipe.getServings(),
                        recipe.getDifficulty().name(),
                        recipe.isPublished(),
                        recipe.getUpdatedAt().toInstant(),
                        ingredientsByRecipeId.getOrDefault(recipe.getRecipeId(), List.of()),
                        new Popularity(0, scrapCountByRecipeId.getOrDefault(recipe.getRecipeId(), 0L), 0),
                        new Rating(null, 0)))
                .toList();

        String nextCursor = hasNext ? encodeCursor(recipes.get(recipes.size() - 1).getRecipeId()) : null;
        long total = updatedAfter == null
                ? recipeRepository.count()
                : recipeRepository.countByUpdatedAtAfter(updatedAfter);
        return new AiRecipeListResponseDto(items, nextCursor, total);
    }

    private int resolveLimit(Integer requestedLimit) {
        if (requestedLimit == null) {
            return DEFAULT_LIMIT;
        }
        if (requestedLimit < 1) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT);
        }
        return Math.min(requestedLimit, MAX_LIMIT);
    }

    private long decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return 0L;
        }
        try {
            String decoded = new String(Base64.getDecoder().decode(cursor), StandardCharsets.UTF_8);
            Matcher matcher = CURSOR_ID.matcher(decoded);
            if (!matcher.find()) {
                throw new BusinessException(CommonErrorCode.INVALID_INPUT);
            }
            return Long.parseLong(matcher.group(1));
        } catch (IllegalArgumentException e) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT);
        }
    }

    private String encodeCursor(Long lastRecipeId) {
        String json = "{\"id\":" + lastRecipeId + "}";
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }
}

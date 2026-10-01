package com.pantrymate.pantryrecipe.recipe.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

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
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecipeRecommendServiceTest {

    private static final Long USER_ID = 1L;
    private static final int SIZE = 20;

    @Mock RecommendationPort recommendationPort;
    @Mock UserAllergyPort userAllergyPort;
    @Mock PantryItemRepository pantryItemRepository;
    @Mock RecipeRepository recipeRepository;
    @Mock RecipePopularityRepository recipePopularityRepository;
    @Mock IngredientRepository ingredientRepository;

    @InjectMocks RecipeRecommendService recipeRecommendService;

    @Test
    void AI_추천이_성공하면_source가_AI이고_항목이_매핑된다() {
        given(userAllergyPort.getAllergies(USER_ID)).willReturn(List.of());
        given(pantryItemRepository.findCookableNonStapleByUserId(USER_ID)).willReturn(List.of());

        Recipe recipe = mockRecipe(10L, true);
        given(recipeRepository.findAllById(List.of(10L))).willReturn(List.of(recipe));

        Ingredient missingIngredient = mockIngredient(99L, "소고기");
        given(ingredientRepository.findAllById(List.of(99L))).willReturn(List.of(missingIngredient));

        RecommendedItem recommendedItem = new RecommendedItem(10L, 1, "임박 재료 활용", 0.8, 1, List.of(99L));
        given(recommendationPort.recommend(any()))
                .willReturn(new Recommendation("req-1", "reco-b-linear-v0", List.of(recommendedItem)));

        RecipeRecommendResponseDto response = recipeRecommendService.recommend(USER_ID, SIZE, null, true);

        assertThat(response.source()).isEqualTo("AI");
        assertThat(response.requestId()).isEqualTo("req-1");
        assertThat(response.items()).hasSize(1);
        Item item = response.items().get(0);
        assertThat(item.rank()).isEqualTo(1);
        assertThat(item.coverage()).isEqualTo(0.8);
        assertThat(item.missingCount()).isEqualTo(1);
        assertThat(item.missingIngredients()).containsExactly(new MissingIngredient(99L, "소고기"));
        assertThat(item.recipe().recipeId()).isEqualTo(10L);
    }

    @Test
    void AI_호출이_실패하면_인기순으로_대체한다() {
        given(userAllergyPort.getAllergies(USER_ID)).willReturn(List.of("새우"));
        given(pantryItemRepository.findCookableNonStapleByUserId(USER_ID)).willReturn(List.of());
        given(recommendationPort.recommend(any())).willThrow(new RecommendationUnavailableException("AI 다운"));

        Recipe fallbackRecipe = mockRecipeId(5L);
        given(recipePopularityRepository.findPopularRecipeIds(1, "새우", null, SIZE)).willReturn(List.of(5L));
        given(recipeRepository.findAllById(List.of(5L))).willReturn(List.of(fallbackRecipe));

        RecipeRecommendResponseDto response = recipeRecommendService.recommend(USER_ID, SIZE, null, true);

        assertThat(response.source()).isEqualTo("POPULARITY");
        assertThat(response.requestId()).isNull();
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).recipe().recipeId()).isEqualTo(5L);
    }

    @Test
    void AI_결과가_비어있으면_인기순으로_대체한다() {
        given(userAllergyPort.getAllergies(USER_ID)).willReturn(List.of());
        given(pantryItemRepository.findCookableNonStapleByUserId(USER_ID)).willReturn(List.of());

        // AI는 추천을 줬지만 해당 레시피가 비공개 등으로 걸러져 표시할 항목이 없는 상황
        RecommendedItem recommendedItem = new RecommendedItem(10L, 1, null, null, 0, List.of());
        given(recommendationPort.recommend(any()))
                .willReturn(new Recommendation("req-2", "v", List.of(recommendedItem)));
        given(recipeRepository.findAllById(List.of(10L))).willReturn(List.of());

        given(recipePopularityRepository.findPopularRecipeIds(0, "", null, SIZE)).willReturn(List.of());

        RecipeRecommendResponseDto response = recipeRecommendService.recommend(USER_ID, SIZE, null, true);

        assertThat(response.source()).isEqualTo("POPULARITY");
        assertThat(response.items()).isEmpty();
    }

    @Test
    void includePantry가_false면_팬트리를_비운_채로_AI에_전달한다() {
        given(userAllergyPort.getAllergies(USER_ID)).willReturn(List.of());
        given(recommendationPort.recommend(any())).willReturn(new Recommendation("req-3", "v", List.of()));
        given(recipePopularityRepository.findPopularRecipeIds(anyInt(), anyString(), any(), anyInt()))
                .willReturn(List.of());

        recipeRecommendService.recommend(USER_ID, SIZE, null, false);

        verify(pantryItemRepository, never()).findCookableNonStapleByUserId(any());
        ArgumentCaptor<RecommendationQuery> captor = ArgumentCaptor.forClass(RecommendationQuery.class);
        verify(recommendationPort).recommend(captor.capture());
        assertThat(captor.getValue().pantry()).isEmpty();
    }

    @Test
    void includePantry가_true면_보유_팬트리를_AI에_전달한다() {
        given(userAllergyPort.getAllergies(USER_ID)).willReturn(List.of());
        PantryItem pantryItem = mockPantryItem(7L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 10));
        given(pantryItemRepository.findCookableNonStapleByUserId(USER_ID)).willReturn(List.of(pantryItem));

        given(recommendationPort.recommend(any())).willReturn(new Recommendation("req-4", "v", List.of()));
        given(recipePopularityRepository.findPopularRecipeIds(anyInt(), anyString(), any(), anyInt()))
                .willReturn(List.of());

        recipeRecommendService.recommend(USER_ID, SIZE, null, true);

        ArgumentCaptor<RecommendationQuery> captor = ArgumentCaptor.forClass(RecommendationQuery.class);
        verify(recommendationPort).recommend(captor.capture());
        assertThat(captor.getValue().pantry())
                .containsExactly(new PantryEntry(7L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 10)));
    }

    @Test
    void 알레르기_조회가_실패하면_추천을_제공하지_않고_예외를_던진다() {
        given(userAllergyPort.getAllergies(USER_ID)).willThrow(new UserAllergyUnavailableException("user-service 다운"));

        assertThatThrownBy(() -> recipeRecommendService.recommend(USER_ID, SIZE, null, true))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(RecipeErrorCode.RECIPE_UNAVAILABLE_RECOMMEND);

        verifyNoInteractions(recommendationPort);
        verifyNoInteractions(pantryItemRepository);
    }

    private Recipe mockRecipe(Long recipeId, boolean published) {
        Recipe recipe = mock(Recipe.class);
        given(recipe.getRecipeId()).willReturn(recipeId);
        given(recipe.isPublished()).willReturn(published);
        return recipe;
    }

    /** 인기순 폴백 경로는 isPublished()를 확인하지 않으므로 recipeId만 스텁한다. */
    private Recipe mockRecipeId(Long recipeId) {
        Recipe recipe = mock(Recipe.class);
        given(recipe.getRecipeId()).willReturn(recipeId);
        return recipe;
    }

    private Ingredient mockIngredient(Long ingredientId, String name) {
        Ingredient ingredient = mock(Ingredient.class);
        given(ingredient.getIngredientId()).willReturn(ingredientId);
        given(ingredient.getName()).willReturn(name);
        return ingredient;
    }

    private PantryItem mockPantryItem(Long ingredientId, LocalDate purchaseDate, LocalDate expiryDate) {
        Ingredient ingredient = mock(Ingredient.class);
        given(ingredient.getIngredientId()).willReturn(ingredientId);
        PantryItem item = mock(PantryItem.class);
        given(item.getIngredient()).willReturn(ingredient);
        given(item.getPurchaseDate()).willReturn(purchaseDate);
        given(item.getExpiryDate()).willReturn(expiryDate);
        return item;
    }
}

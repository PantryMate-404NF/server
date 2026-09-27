package com.pantrymate.pantryrecipe.recipe.domain;

public record RecipeIngredientRow(Long recipeId, Long ingredientId, String name, boolean main, String unit) {}

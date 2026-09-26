package com.pantrymate.pantryrecipe.ingredient.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    Optional<Ingredient> findByNameIgnoreCase(String name);

    List<Ingredient> findAllByOrderByIngredientIdAsc();
}

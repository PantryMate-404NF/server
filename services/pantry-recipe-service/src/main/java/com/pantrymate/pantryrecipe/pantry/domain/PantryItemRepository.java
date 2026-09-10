package com.pantrymate.pantryrecipe.pantry.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PantryItemRepository extends JpaRepository<PantryItem, Long> {
}

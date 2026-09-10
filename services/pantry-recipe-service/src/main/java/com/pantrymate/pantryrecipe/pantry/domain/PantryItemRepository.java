package com.pantrymate.pantryrecipe.pantry.domain;

import com.pantrymate.pantryrecipe.ingredient.domain.enums.StorageType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PantryItemRepository extends JpaRepository<PantryItem, Long> {

    List<PantryItem> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<PantryItem> findByUserIdAndStorageTypeOrderByCreatedAtDesc(Long userId, StorageType storageType);
}

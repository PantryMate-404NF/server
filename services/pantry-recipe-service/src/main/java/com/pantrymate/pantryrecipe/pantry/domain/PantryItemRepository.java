package com.pantrymate.pantryrecipe.pantry.domain;

import com.pantrymate.pantryrecipe.ingredient.domain.enums.StorageType;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PantryItemRepository extends JpaRepository<PantryItem, Long> {

    List<PantryItem> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<PantryItem> findByUserIdAndIngredient_IngredientIdIn(Long userId, Collection<Long> ingredientIds);

    List<PantryItem> findByUserIdAndIngredientIsNotNull(Long userId);

    @Query("SELECT DISTINCT p.userId FROM PantryItem p")
    List<Long> findDistinctUserIds();

    List<PantryItem> findByUserIdAndStorageTypeOrderByCreatedAtDesc(Long userId, StorageType storageType);

    List<PantryItem> findByUserIdOrderByCreatedAtAsc(Long userId);

    List<PantryItem> findByUserIdAndStorageTypeOrderByCreatedAtAsc(Long userId, StorageType storageType);

    @Query("""
            SELECT p FROM PantryItem p
            WHERE p.userId = :userId
            ORDER BY
              CASE WHEN p.expiryDate < CURRENT_DATE THEN 0 ELSE 1 END ASC,
              p.expiryDate ASC,
              p.createdAt ASC
            """)
    List<PantryItem> findByUserIdOrderByImminent(@Param("userId") Long userId);

    @Query("""
            SELECT p FROM PantryItem p
            WHERE p.userId = :userId
              AND p.storageType = :storageType
            ORDER BY
              CASE WHEN p.expiryDate < CURRENT_DATE THEN 0 ELSE 1 END ASC,
              p.expiryDate ASC,
              p.createdAt ASC
            """)
    List<PantryItem> findByUserIdAndStorageTypeOrderByImminent(
            @Param("userId") Long userId, @Param("storageType") StorageType storageType);

    @Query(
            value =
                    "SELECT pantry_item_id FROM pantry_items "
                            + "WHERE user_id = :userId AND to_tsvector('simple', name) @@ plainto_tsquery('simple', :keyword)",
            nativeQuery = true)
    Set<Long> findMatchingIdsByUserIdAndKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);
}

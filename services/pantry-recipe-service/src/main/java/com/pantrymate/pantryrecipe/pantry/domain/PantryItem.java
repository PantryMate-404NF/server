package com.pantrymate.pantryrecipe.pantry.domain;

import com.pantrymate.pantryrecipe.ingredient.domain.Ingredient;
import com.pantrymate.pantryrecipe.ingredient.domain.enums.StorageType;
import com.pantrymate.pantryrecipe.pantry.domain.enums.PantryRegisterType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "pantry_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PantryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pantry_item_id")
    private Long pantryItemId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "storage_type", nullable = false)
    private StorageType storageType;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "sell_by_date")
    private LocalDate sellByDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "is_expiry_auto_calculated", nullable = false)
    private boolean expiryAutoCalculated;

    @Column(name = "is_cookable", nullable = false)
    private boolean cookable;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "register_type", nullable = false)
    private PantryRegisterType registerType;

    @Column(name = "order_item_id", unique = true)
    private Long orderItemId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public static PantryItem createManual(
            Long userId,
            String name,
            String imageUrl,
            StorageType storageType,
            LocalDate expiryDate,
            boolean expiryAutoCalculated) {
        PantryItem pantryItem = new PantryItem();
        pantryItem.userId = userId;
        pantryItem.name = name;
        pantryItem.imageUrl = imageUrl;
        pantryItem.storageType = storageType;
        pantryItem.expiryDate = expiryDate;
        pantryItem.expiryAutoCalculated = expiryAutoCalculated;
        pantryItem.cookable = true;
        pantryItem.registerType = PantryRegisterType.MANUAL;
        return pantryItem;
    }
}

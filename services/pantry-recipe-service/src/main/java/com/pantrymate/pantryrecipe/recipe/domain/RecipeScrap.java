package com.pantrymate.pantryrecipe.recipe.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "recipe_scraps",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "recipe_id"})})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecipeScrap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipe_scrap_id")
    private Long recipeScrapId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = OffsetDateTime.now();
    }

    public static RecipeScrap create(Long userId, Recipe recipe) {
        RecipeScrap scrap = new RecipeScrap();
        scrap.userId = userId;
        scrap.recipe = recipe;
        return scrap;
    }
}

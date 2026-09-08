package com.pantrymate.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "user_preferences")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "preference_id")
    private Long preferenceId;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "family_member_count", nullable = false)
    private Integer familyMemberCount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preferred_food_types", columnDefinition = "jsonb")
    private List<String> preferredFoodTypes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> allergies;

    @Column(name = "onboarding_completed", nullable = false)
    private boolean onboardingCompleted;

    @Column(name = "onboarding_step", nullable = false)
    private Integer onboardingStep;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    private UserPreference(Long userId) {
        this.userId = userId;
        this.familyMemberCount = 1;
        this.onboardingCompleted = false;
        this.onboardingStep = 1;
    }

    public static UserPreference createFor(Long userId) {
        return new UserPreference(userId);
    }

    public void update(
            Integer familyMemberCount,
            List<String> preferredFoodTypes,
            List<String> allergies,
            boolean onboardingCompleted,
            Integer onboardingStep) {
        this.familyMemberCount = familyMemberCount;
        this.preferredFoodTypes = preferredFoodTypes;
        this.allergies = allergies;
        this.onboardingCompleted = onboardingCompleted;
        this.onboardingStep = onboardingStep;
    }

    @PrePersist
    @PreUpdate
    void touch() {
        this.updatedAt = OffsetDateTime.now();
    }
}

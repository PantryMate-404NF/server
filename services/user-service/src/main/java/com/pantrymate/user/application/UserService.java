package com.pantrymate.user.application;

import com.pantrymate.common.exception.BusinessException;
import com.pantrymate.user.domain.OnboardingPort;
import com.pantrymate.user.domain.OnboardingPort.OnboardingSnapshot;
import com.pantrymate.user.domain.OnboardingPort.OnboardingUnavailableException;
import com.pantrymate.user.domain.User;
import com.pantrymate.user.domain.UserPreference;
import com.pantrymate.user.domain.UserPreferenceRepository;
import com.pantrymate.user.domain.UserPreferenceUpdatedEvent;
import com.pantrymate.user.domain.UserRepository;
import com.pantrymate.user.domain.exception.UserErrorCode;
import com.pantrymate.user.presentation.dto.OnboardingFoodListResponseDto;
import com.pantrymate.user.presentation.dto.TastePreferenceDto;
import com.pantrymate.user.presentation.dto.UserPreferenceResponseDto;
import com.pantrymate.user.presentation.dto.UserPreferenceUpdateRequestDto;
import com.pantrymate.user.presentation.dto.UserProfileResponseDto;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final int MAX_FAMILY_MEMBER_COUNT = 10;
    private static final int MAX_PREFERRED_FOOD_TYPES = 5;
    private static final Set<String> ALLOWED_ALLERGIES = Set.of(
            "알류(가금류)", "우유", "메밀", "땅콩", "대두", "밀", "고등어", "게", "새우", "돼지고기",
            "복숭아", "토마토", "아황산류", "호두", "닭고기", "쇠고기", "오징어", "조개류(굴,전복,홍합 포함)", "잣");
    private static final int MIN_FAVORITE_FOODS = 3;
    private static final int MAX_FAVORITE_FOODS = 10;
    private static final int MIN_TASTE_LEVEL = 1;
    private static final int MAX_TASTE_LEVEL = 5;
    private static final Set<String> ALLOWED_PREFERRED_FOOD_TYPES =
            Set.of("KOREAN", "WESTERN", "JAPANESE", "CHINESE", "ETC");

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final OnboardingPort onboardingPort;
    private final ApplicationEventPublisher eventPublisher;

    public UserService(
            UserRepository userRepository,
            UserPreferenceRepository userPreferenceRepository,
            OnboardingPort onboardingPort,
            ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.userPreferenceRepository = userPreferenceRepository;
        this.onboardingPort = onboardingPort;
        this.eventPublisher = eventPublisher;
    }

    /** 화면을 그리기 직전의 목록이 필요하므로 캐시하지 않고 매번 AI에서 받는다. */
    public OnboardingFoodListResponseDto getOnboardingFoods() {
        try {
            return OnboardingFoodListResponseDto.from(onboardingPort.getPresentedFoods());
        } catch (OnboardingUnavailableException e) {
            throw new BusinessException(UserErrorCode.ONBOARD_UNAVAILABLE_FOODS);
        }
    }

    @Transactional(readOnly = true)
    public UserProfileResponseDto getProfile(Long userId) {
        User user = getUserByIdOrThrow(userId);
        boolean onboardingCompleted = userPreferenceRepository
                .findByUserId(userId)
                .map(UserPreference::isOnboardingCompleted)
                .orElse(false);
        return UserProfileResponseDto.of(user, onboardingCompleted);
    }

    @Transactional(readOnly = true)
    public UserPreferenceResponseDto getPreferences(Long userId) {
        getUserByIdOrThrow(userId);
        UserPreference preference = userPreferenceRepository
                .findByUserId(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.ONBOARD_NOTFOUND_PREFERENCE));
        return UserPreferenceResponseDto.from(preference);
    }

    @Transactional
    public UserPreferenceResponseDto savePreferences(Long userId, UserPreferenceUpdateRequestDto request) {
        getUserByIdOrThrow(userId);
        validate(request);

        UserPreference preference =
                userPreferenceRepository.findByUserId(userId).orElseGet(() -> UserPreference.createFor(userId));

        TastePreferenceDto tastePreferences = request.tastePreferences();
        preference.update(
                request.familyMemberCount(),
                request.preferredFoodTypes(),
                request.allergies(),
                request.favoriteFoods(),
                tastePreferences != null ? tastePreferences.salty() : null,
                tastePreferences != null ? tastePreferences.sweet() : null,
                tastePreferences != null ? tastePreferences.spicy() : null,
                Boolean.TRUE.equals(request.onboardingCompleted()),
                request.onboardingStep());

        UserPreference saved = userPreferenceRepository.save(preference);
        publishOnboardingSnapshot(userId, saved);
        return UserPreferenceResponseDto.from(saved);
    }

    /** 고른 음식이 있을 때만 AI에 전달한다(AI는 picks 1개 이상이 필요). 전달은 커밋 이후에 하고 실패해도 저장에는 영향이 없다. */
    private void publishOnboardingSnapshot(Long userId, UserPreference saved) {
        List<String> picks = saved.getFavoriteFoods();
        if (picks == null || picks.isEmpty()) {
            return;
        }
        eventPublisher.publishEvent(new UserPreferenceUpdatedEvent(new OnboardingSnapshot(
                userId,
                picks,
                saved.getTasteSpicy(),
                saved.getTasteSalty(),
                saved.getTasteSweet(),
                saved.getPreferredFoodTypes(),
                saved.getAllergies(),
                saved.getFamilyMemberCount())));
    }

    private void validate(UserPreferenceUpdateRequestDto request) {
        if (request.familyMemberCount() == null
                || request.familyMemberCount() < 1
                || request.familyMemberCount() > MAX_FAMILY_MEMBER_COUNT
                || request.onboardingCompleted() == null
                || request.onboardingStep() == null
                || request.onboardingStep() < 1) {
            throw new BusinessException(UserErrorCode.ONBOARD_INVALID_INPUT);
        }
        validatePreferredFoodTypes(request.preferredFoodTypes());
        validateAllergies(request.allergies());
        validateFavoriteFoods(request.favoriteFoods());
        validateTastePreferences(request.tastePreferences());
    }

    private void validatePreferredFoodTypes(List<String> preferredFoodTypes) {
        if (preferredFoodTypes == null) {
            return;
        }
        if (preferredFoodTypes.size() > MAX_PREFERRED_FOOD_TYPES
                || preferredFoodTypes.contains(null)
                || !ALLOWED_PREFERRED_FOOD_TYPES.containsAll(preferredFoodTypes)
                || new HashSet<>(preferredFoodTypes).size() != preferredFoodTypes.size()) {
            throw new BusinessException(UserErrorCode.ONBOARD_INVALID_INPUT);
        }
    }

    private void validateAllergies(List<String> allergies) {
        if (allergies == null) {
            return;
        }
        if (allergies.contains(null)
                || !ALLOWED_ALLERGIES.containsAll(allergies)
                || new HashSet<>(allergies).size() != allergies.size()) {
            throw new BusinessException(UserErrorCode.ONBOARD_INVALID_INPUT);
        }
    }

    private void validateFavoriteFoods(List<String> favoriteFoods) {
        if (favoriteFoods == null || favoriteFoods.isEmpty()) {
            return;
        }
        if (favoriteFoods.size() < MIN_FAVORITE_FOODS
                || favoriteFoods.size() > MAX_FAVORITE_FOODS
                || new HashSet<>(favoriteFoods).size() != favoriteFoods.size()) {
            throw new BusinessException(UserErrorCode.ONBOARD_INVALID_INPUT);
        }
    }

    private void validateTastePreferences(TastePreferenceDto tastePreferences) {
        if (tastePreferences == null) {
            return;
        }
        if (!isValidTasteLevel(tastePreferences.salty())
                || !isValidTasteLevel(tastePreferences.sweet())
                || !isValidTasteLevel(tastePreferences.spicy())) {
            throw new BusinessException(UserErrorCode.ONBOARD_INVALID_INPUT);
        }
    }

    private boolean isValidTasteLevel(Integer level) {
        return level != null && level >= MIN_TASTE_LEVEL && level <= MAX_TASTE_LEVEL;
    }

    private User getUserByIdOrThrow(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOTFOUND_ID));
    }
}

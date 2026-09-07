package com.pantrymate.user.application;

import com.pantrymate.common.exception.BusinessException;
import com.pantrymate.user.domain.User;
import com.pantrymate.user.domain.UserPreference;
import com.pantrymate.user.domain.UserPreferenceRepository;
import com.pantrymate.user.domain.UserRepository;
import com.pantrymate.user.domain.exception.UserErrorCode;
import com.pantrymate.user.presentation.dto.UserPreferenceResponseDto;
import com.pantrymate.user.presentation.dto.UserPreferenceUpdateRequestDto;
import com.pantrymate.user.presentation.dto.UserProfileResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    public UserService(UserRepository userRepository, UserPreferenceRepository userPreferenceRepository) {
        this.userRepository = userRepository;
        this.userPreferenceRepository = userPreferenceRepository;
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

    @Transactional
    public UserPreferenceResponseDto savePreferences(Long userId, UserPreferenceUpdateRequestDto request) {
        getUserByIdOrThrow(userId);
        validate(request);

        UserPreference preference =
                userPreferenceRepository.findByUserId(userId).orElseGet(() -> UserPreference.createFor(userId));

        preference.update(
                request.familyMemberCount(),
                request.preferredFoodTypes(),
                request.allergies(),
                Boolean.TRUE.equals(request.onboardingCompleted()),
                request.onboardingStep());

        UserPreference saved = userPreferenceRepository.save(preference);
        return UserPreferenceResponseDto.from(saved);
    }

    private void validate(UserPreferenceUpdateRequestDto request) {
        if (request.familyMemberCount() == null
                || request.familyMemberCount() < 1
                || request.onboardingCompleted() == null
                || request.onboardingStep() == null) {
            throw new BusinessException(UserErrorCode.ONBOARD_INVALID_INPUT);
        }
    }

    private User getUserByIdOrThrow(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOTFOUND_ID));
    }
}

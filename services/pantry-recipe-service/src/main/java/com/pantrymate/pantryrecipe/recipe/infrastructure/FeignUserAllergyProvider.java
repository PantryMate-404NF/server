package com.pantrymate.pantryrecipe.recipe.infrastructure;

import com.pantrymate.common.dto.ApiResponse;
import com.pantrymate.pantryrecipe.recipe.domain.UserAllergyPort;
import com.pantrymate.pantryrecipe.recipe.infrastructure.UserServiceClient.PreferencePayload;
import feign.FeignException;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class FeignUserAllergyProvider implements UserAllergyPort {

    private final UserServiceClient client;

    public FeignUserAllergyProvider(UserServiceClient client) {
        this.client = client;
    }

    @Override
    public List<String> getAllergies(Long userId) {
        try {
            ApiResponse<PreferencePayload> response = client.getPreferences(userId);
            PreferencePayload payload = response == null ? null : response.data();
            return payload == null || payload.allergies() == null ? List.of() : payload.allergies();
        } catch (FeignException.NotFound e) {
            // 온보딩을 시작하지 않은 사용자는 저장된 알레르기가 없다.
            return List.of();
        } catch (RuntimeException e) {
            throw new UserAllergyUnavailableException("알레르기 정보를 조회하지 못함: " + e.getMessage());
        }
    }
}

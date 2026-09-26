package com.pantrymate.pantryrecipe.recipe.infrastructure;

import com.pantrymate.common.dto.ApiResponse;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service", url = "${user-service.url}")
public interface UserServiceClient {

    @GetMapping("/api/users/me/preferences")
    ApiResponse<PreferencePayload> getPreferences(@RequestHeader("X-User-Id") Long userId);

    record PreferencePayload(List<String> allergies) {}
}

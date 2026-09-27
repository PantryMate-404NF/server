package com.pantrymate.notification.reminder.infrastructure;

import com.pantrymate.common.dto.ApiResponse;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "pantry-recipe-service", url = "${pantry-recipe.service-uri}")
public interface PantryRecipeClient {

    @GetMapping("/internal/pantry-items/user-ids")
    ApiResponse<List<Long>> listPantryItemUserIds();
}

package com.pantrymate.pantryrecipe.pantry.presentation.controller;

import com.pantrymate.common.dto.ApiResponse;
import com.pantrymate.pantryrecipe.pantry.application.PantryItemService;
import io.swagger.v3.oas.annotations.Hidden;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/* 서비스 간 내부 호출 전용 */
@Hidden
@RestController
@RequestMapping("/internal/pantry-items")
public class InternalPantryItemController {

    private final PantryItemService pantryItemService;

    public InternalPantryItemController(PantryItemService pantryItemService) {
        this.pantryItemService = pantryItemService;
    }

    @GetMapping("/user-ids")
    public ResponseEntity<ApiResponse<List<Long>>> listUserIds() {
        List<Long> response = pantryItemService.getUserIdsWithItems();
        return ResponseEntity.ok(ApiResponse.success("팬트리 재료 보유 유저 조회가 완료되었습니다.", response));
    }
}

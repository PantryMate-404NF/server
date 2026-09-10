package com.pantrymate.pantryrecipe.pantry.presentation.controller;

import com.pantrymate.common.dto.ApiResponse;
import com.pantrymate.common.dto.CurrentUser;
import com.pantrymate.pantryrecipe.pantry.application.PantryItemService;
import com.pantrymate.pantryrecipe.pantry.presentation.dto.PantryItemCreateRequestDto;
import com.pantrymate.pantryrecipe.pantry.presentation.dto.PantryItemResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "PANTRY", description = "팬트리 식재료 관리")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/pantry-items")
public class PantryItemController {

    private final PantryItemService pantryItemService;

    public PantryItemController(PantryItemService pantryItemService) {
        this.pantryItemService = pantryItemService;
    }

    @Operation(summary = "팬트리 식재료 수기 등록", description = "소비기한 미입력 시 등록일 기준으로 자동 계산된다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "등록 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "PANTRY-INVALID-NAME / PANTRY-INVALID-DATE / PANTRY-INVALID-STORAGE"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUTH-UNAUTHORIZED")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<PantryItemResponseDto>> create(
            @Parameter(hidden = true) CurrentUser currentUser, @RequestBody PantryItemCreateRequestDto request) {
        PantryItemResponseDto response = pantryItemService.save(currentUser.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("팬트리에 식재료가 성공적으로 등록되었습니다.", response));
    }
}

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
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @Operation(summary = "팬트리 식재료 목록 조회", description = "로그인한 유저의 팬트리 식재료를 최근 등록순으로 조회한다. 보관방법 필터링을 지원한다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400", description = "PANTRY-INVALID-STORAGE"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUTH-UNAUTHORIZED")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<PantryItemResponseDto>>> list(
            @Parameter(hidden = true) CurrentUser currentUser,
            @Parameter(description = "보관방법 필터 (REFRIGERATED / FROZEN / ROOM_TEMP)")
                    @RequestParam(required = false)
                    String storageType) {
        List<PantryItemResponseDto> response = pantryItemService.getAll(currentUser.userId(), storageType);
        return ResponseEntity.ok(ApiResponse.success("팬트리 목록 조회가 완료되었습니다.", response));
    }
}

package com.pantrymate.pantryrecipe.pantry.presentation.controller;

import com.pantrymate.common.dto.ApiResponse;
import com.pantrymate.common.dto.CurrentUser;
import com.pantrymate.pantryrecipe.pantry.application.PantryItemService;
import com.pantrymate.pantryrecipe.pantry.presentation.dto.PantryItemCreateRequestDto;
import com.pantrymate.pantryrecipe.pantry.presentation.dto.PantryItemResponseDto;
import com.pantrymate.pantryrecipe.pantry.presentation.dto.PantryItemUpdateRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @Operation(
            summary = "팬트리 식재료 수기 등록",
            description = "소비기한 미입력 시 유통기한 기준(임시 +7일)으로, 유통기한도 미입력 시 등록일 기준으로 자동 계산된다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "201",
                description = "등록 성공",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                                                        {
                                                          "status": "SUCCESS",
                                                          "message": "팬트리에 식재료가 성공적으로 등록되었습니다.",
                                                          "data": {
                                                            "pantryItemId": 1,
                                                            "ingredientName": "양파",
                                                            "sellByDate": null,
                                                            "expiryDate": "2026-09-20",
                                                            "dDay": 4,
                                                            "expiryStatus": "NORMAL",
                                                            "storageType": "REFRIGERATED",
                                                            "isExpiryAutoCalculated": false,
                                                            "isCookable": true,
                                                            "registerType": "MANUAL",
                                                            "imageUrl": null
                                                          },
                                                          "error": null,
                                                          "timestamp": "2026-09-16T01:23:45.678Z"
                                                        }
                                                        """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "PANTRY-INVALID-NAME / PANTRY-INVALID-DATE / PANTRY-INVALID-STORAGE",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                examples = {
                                    @ExampleObject(
                                            name = "PANTRY-INVALID-NAME",
                                            value =
                                                    """
                                                    {
                                                      "status": "ERROR",
                                                      "message": "식재료명은 1자 이상 20자 이하여야 합니다.",
                                                      "data": null,
                                                      "error": "PANTRY-INVALID-NAME",
                                                      "timestamp": "2026-09-16T01:23:45.678Z"
                                                    }
                                                    """),
                                    @ExampleObject(
                                            name = "PANTRY-INVALID-DATE",
                                            value =
                                                    """
                                                    {
                                                      "status": "ERROR",
                                                      "message": "올바른 날짜 형식(YYYY-MM-DD)을 입력해주세요.",
                                                      "data": null,
                                                      "error": "PANTRY-INVALID-DATE",
                                                      "timestamp": "2026-09-16T01:23:45.678Z"
                                                    }
                                                    """),
                                    @ExampleObject(
                                            name = "PANTRY-INVALID-STORAGE",
                                            value =
                                                    """
                                                    {
                                                      "status": "ERROR",
                                                      "message": "올바른 보관방법(REFRIGERATED, FROZEN, ROOM_TEMP)을 선택해주세요.",
                                                      "data": null,
                                                      "error": "PANTRY-INVALID-STORAGE",
                                                      "timestamp": "2026-09-16T01:23:45.678Z"
                                                    }
                                                    """)
                                })),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "AUTH-UNAUTHORIZED",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                                                        {
                                                          "status": "ERROR",
                                                          "message": "인증 정보가 유효하지 않습니다.",
                                                          "data": null,
                                                          "error": "AUTH-UNAUTHORIZED",
                                                          "timestamp": "2026-09-16T01:23:45.678Z"
                                                        }
                                                        """)))
    })
    @PostMapping
    public ResponseEntity<ApiResponse<PantryItemResponseDto>> create(
            @Parameter(hidden = true) CurrentUser currentUser, @RequestBody PantryItemCreateRequestDto request) {
        PantryItemResponseDto response = pantryItemService.save(currentUser.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("팬트리에 식재료가 성공적으로 등록되었습니다.", response));
    }

    @Operation(summary = "팬트리 식재료 목록 조회", description = "로그인한 유저의 팬트리 식재료를 조회한다. 보관방법 필터링과 정렬 기준 선택을 지원한다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                                                        {
                                                          "status": "SUCCESS",
                                                          "message": "팬트리 목록 조회가 완료되었습니다.",
                                                          "data": [
                                                            {
                                                              "pantryItemId": 1,
                                                              "ingredientName": "양파",
                                                              "sellByDate": null,
                                                              "expiryDate": "2026-09-20",
                                                              "dDay": 4,
                                                              "expiryStatus": "NORMAL",
                                                              "storageType": "REFRIGERATED",
                                                              "isExpiryAutoCalculated": false,
                                                              "isCookable": true,
                                                              "registerType": "MANUAL",
                                                              "imageUrl": null
                                                            }
                                                          ],
                                                          "error": null,
                                                          "timestamp": "2026-09-16T01:23:45.678Z"
                                                        }
                                                        """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "PANTRY-INVALID-STORAGE / PANTRY-INVALID-SORT",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                examples = {
                                    @ExampleObject(
                                            name = "PANTRY-INVALID-STORAGE",
                                            value =
                                                    """
                                                    {
                                                      "status": "ERROR",
                                                      "message": "올바른 보관방법(REFRIGERATED, FROZEN, ROOM_TEMP)을 선택해주세요.",
                                                      "data": null,
                                                      "error": "PANTRY-INVALID-STORAGE",
                                                      "timestamp": "2026-09-16T01:23:45.678Z"
                                                    }
                                                    """),
                                    @ExampleObject(
                                            name = "PANTRY-INVALID-SORT",
                                            value =
                                                    """
                                                    {
                                                      "status": "ERROR",
                                                      "message": "올바른 정렬 기준(RECENT, IMMINENT, OLDEST)을 선택해주세요.",
                                                      "data": null,
                                                      "error": "PANTRY-INVALID-SORT",
                                                      "timestamp": "2026-09-16T01:23:45.678Z"
                                                    }
                                                    """)
                                })),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "AUTH-UNAUTHORIZED",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                                                        {
                                                          "status": "ERROR",
                                                          "message": "인증 정보가 유효하지 않습니다.",
                                                          "data": null,
                                                          "error": "AUTH-UNAUTHORIZED",
                                                          "timestamp": "2026-09-16T01:23:45.678Z"
                                                        }
                                                        """)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<PantryItemResponseDto>>> list(
            @Parameter(hidden = true) CurrentUser currentUser,
            @Parameter(description = "보관방법 필터 (REFRIGERATED / FROZEN / ROOM_TEMP)")
                    @RequestParam(required = false)
                    String storageType,
            @Parameter(description = "정렬 기준 (RECENT: 최근 등록순(기본값) / IMMINENT: 소비기한 임박순 / OLDEST: 오래된 등록순)")
                    @RequestParam(required = false)
                    String sort) {
        List<PantryItemResponseDto> response = pantryItemService.getAll(currentUser.userId(), storageType, sort);
        return ResponseEntity.ok(ApiResponse.success("팬트리 목록 조회가 완료되었습니다.", response));
    }

    @Operation(
            summary = "팬트리 식재료 수정",
            description = "수동 등록 항목은 식재료명·유통기한·보관방법·이미지·요리가능여부를 모두 수정할 수 있다. "
                    + "자동 등록 항목은 식재료명·보관방법·이미지가 SKU에 연결되어 있어 수정되지 않고, 유통기한과 요리가능여부만 반영된다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "수정 성공",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                                                        {
                                                          "status": "SUCCESS",
                                                          "message": "팬트리 식재료가 수정되었습니다.",
                                                          "data": {
                                                            "pantryItemId": 1,
                                                            "ingredientName": "양파",
                                                            "sellByDate": null,
                                                            "expiryDate": "2026-09-25",
                                                            "dDay": 9,
                                                            "expiryStatus": "NORMAL",
                                                            "storageType": "REFRIGERATED",
                                                            "isExpiryAutoCalculated": false,
                                                            "isCookable": true,
                                                            "registerType": "MANUAL",
                                                            "imageUrl": null
                                                          },
                                                          "error": null,
                                                          "timestamp": "2026-09-16T01:23:45.678Z"
                                                        }
                                                        """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "PANTRY-INVALID-NAME / PANTRY-INVALID-DATE / PANTRY-INVALID-STORAGE",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                examples = {
                                    @ExampleObject(
                                            name = "PANTRY-INVALID-NAME",
                                            value =
                                                    """
                                                    {
                                                      "status": "ERROR",
                                                      "message": "식재료명은 1자 이상 20자 이하여야 합니다.",
                                                      "data": null,
                                                      "error": "PANTRY-INVALID-NAME",
                                                      "timestamp": "2026-09-16T01:23:45.678Z"
                                                    }
                                                    """),
                                    @ExampleObject(
                                            name = "PANTRY-INVALID-DATE",
                                            value =
                                                    """
                                                    {
                                                      "status": "ERROR",
                                                      "message": "올바른 날짜 형식(YYYY-MM-DD)을 입력해주세요.",
                                                      "data": null,
                                                      "error": "PANTRY-INVALID-DATE",
                                                      "timestamp": "2026-09-16T01:23:45.678Z"
                                                    }
                                                    """),
                                    @ExampleObject(
                                            name = "PANTRY-INVALID-STORAGE",
                                            value =
                                                    """
                                                    {
                                                      "status": "ERROR",
                                                      "message": "올바른 보관방법(REFRIGERATED, FROZEN, ROOM_TEMP)을 선택해주세요.",
                                                      "data": null,
                                                      "error": "PANTRY-INVALID-STORAGE",
                                                      "timestamp": "2026-09-16T01:23:45.678Z"
                                                    }
                                                    """)
                                })),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "AUTH-UNAUTHORIZED",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                                                        {
                                                          "status": "ERROR",
                                                          "message": "인증 정보가 유효하지 않습니다.",
                                                          "data": null,
                                                          "error": "AUTH-UNAUTHORIZED",
                                                          "timestamp": "2026-09-16T01:23:45.678Z"
                                                        }
                                                        """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "PANTRY-NOTFOUND-ITEM",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                                                        {
                                                          "status": "ERROR",
                                                          "message": "해당 팬트리 식재료를 찾을 수 없습니다.",
                                                          "data": null,
                                                          "error": "PANTRY-NOTFOUND-ITEM",
                                                          "timestamp": "2026-09-16T01:23:45.678Z"
                                                        }
                                                        """)))
    })
    @PatchMapping("/{pantryItemId}")
    public ResponseEntity<ApiResponse<PantryItemResponseDto>> edit(
            @Parameter(hidden = true) CurrentUser currentUser,
            @PathVariable Long pantryItemId,
            @RequestBody PantryItemUpdateRequestDto request) {
        PantryItemResponseDto response = pantryItemService.update(currentUser.userId(), pantryItemId, request);
        return ResponseEntity.ok(ApiResponse.success("팬트리 식재료가 수정되었습니다.", response));
    }

    @Operation(summary = "팬트리 식재료 단건 삭제", description = "본인 소유의 팬트리 식재료를 삭제한다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "삭제 성공",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                                                        {
                                                          "status": "SUCCESS",
                                                          "message": "팬트리 식재료가 삭제되었습니다.",
                                                          "data": null,
                                                          "error": null,
                                                          "timestamp": "2026-09-16T01:23:45.678Z"
                                                        }
                                                        """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "AUTH-UNAUTHORIZED",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                                                        {
                                                          "status": "ERROR",
                                                          "message": "인증 정보가 유효하지 않습니다.",
                                                          "data": null,
                                                          "error": "AUTH-UNAUTHORIZED",
                                                          "timestamp": "2026-09-16T01:23:45.678Z"
                                                        }
                                                        """))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "PANTRY-NOTFOUND-ITEM",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                                                        {
                                                          "status": "ERROR",
                                                          "message": "해당 팬트리 식재료를 찾을 수 없습니다.",
                                                          "data": null,
                                                          "error": "PANTRY-NOTFOUND-ITEM",
                                                          "timestamp": "2026-09-16T01:23:45.678Z"
                                                        }
                                                        """)))
    })
    @DeleteMapping("/{pantryItemId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(hidden = true) CurrentUser currentUser, @PathVariable Long pantryItemId) {
        pantryItemService.delete(currentUser.userId(), pantryItemId);
        return ResponseEntity.ok(ApiResponse.success("팬트리 식재료가 삭제되었습니다.", null));
    }
}

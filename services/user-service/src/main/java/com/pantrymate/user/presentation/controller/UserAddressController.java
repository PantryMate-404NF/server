package com.pantrymate.user.presentation.controller;

import com.pantrymate.common.dto.ApiResponse;
import com.pantrymate.common.dto.CurrentUser;
import com.pantrymate.user.application.UserAddressService;
import com.pantrymate.user.presentation.dto.UserAddressCreateRequestDto;
import com.pantrymate.user.presentation.dto.UserAddressResponseDto;
import com.pantrymate.user.presentation.dto.UserAddressUpdateRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "USER-ADDRESS", description = "배송지 관리 (마이페이지 · 주문서 공용)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/users/me/addresses")
public class UserAddressController {

    private final UserAddressService userAddressService;

    public UserAddressController(UserAddressService userAddressService) {
        this.userAddressService = userAddressService;
    }

    @Operation(summary = "배송지 목록 조회", description = "기본 배송지가 맨 앞, 이후 최근 등록순으로 반환한다. 배송지가 없으면 빈 배열이다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserAddressResponseDto>>> list(
            @Parameter(hidden = true) CurrentUser currentUser) {
        List<UserAddressResponseDto> response = userAddressService.getAll(currentUser.userId());
        return ResponseEntity.ok(ApiResponse.success("배송지 목록 조회가 완료되었습니다.", response));
    }

    @Operation(summary = "기본 배송지 조회", description = "주문서 진입 시 기본값으로 사용한다. 기본 배송지가 없으면 data가 null이다.")
    @GetMapping("/default")
    public ResponseEntity<ApiResponse<UserAddressResponseDto>> detailDefault(
            @Parameter(hidden = true) CurrentUser currentUser) {
        UserAddressResponseDto response = userAddressService.getDefault(currentUser.userId());
        return ResponseEntity.ok(ApiResponse.success("기본 배송지 조회가 완료되었습니다.", response));
    }

    @Operation(
            summary = "배송지 등록",
            description = "최대 10개까지 등록할 수 있다. 첫 배송지는 자동으로 기본 배송지가 되고, isDefault=true로 등록하면 기존 기본 배송지는 해제된다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "등록 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400", description = "USER-INVALID-ADDRESS / USER-LIMIT-ADDRESS"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "USER-NOTFOUND-ID")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<UserAddressResponseDto>> create(
            @Parameter(hidden = true) CurrentUser currentUser, @RequestBody UserAddressCreateRequestDto request) {
        UserAddressResponseDto response = userAddressService.save(currentUser.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("배송지가 등록되었습니다.", response));
    }

    @Operation(summary = "배송지 수정", description = "기본 배송지 여부는 바뀌지 않는다. 기본 배송지 변경은 PATCH /{addressId}/default를 사용한다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "USER-INVALID-ADDRESS"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "USER-NOTFOUND-ADDRESS")
    })
    @PatchMapping("/{addressId}")
    public ResponseEntity<ApiResponse<UserAddressResponseDto>> edit(
            @Parameter(hidden = true) CurrentUser currentUser,
            @PathVariable Long addressId,
            @RequestBody UserAddressUpdateRequestDto request) {
        UserAddressResponseDto response = userAddressService.update(currentUser.userId(), addressId, request);
        return ResponseEntity.ok(ApiResponse.success("배송지가 수정되었습니다.", response));
    }

    @Operation(summary = "기본 배송지 설정", description = "지정한 배송지를 기본 배송지로 바꾸고 기존 기본 배송지는 해제한다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "설정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "USER-NOTFOUND-ADDRESS")
    })
    @PatchMapping("/{addressId}/default")
    public ResponseEntity<ApiResponse<UserAddressResponseDto>> editDefault(
            @Parameter(hidden = true) CurrentUser currentUser, @PathVariable Long addressId) {
        UserAddressResponseDto response = userAddressService.updateDefault(currentUser.userId(), addressId);
        return ResponseEntity.ok(ApiResponse.success("기본 배송지가 설정되었습니다.", response));
    }

    @Operation(
            summary = "배송지 삭제",
            description = "기본 배송지를 삭제하면 남은 배송지 중 가장 최근에 등록한 배송지가 기본 배송지가 된다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "USER-NOTFOUND-ADDRESS")
    })
    @DeleteMapping("/{addressId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(hidden = true) CurrentUser currentUser, @PathVariable Long addressId) {
        userAddressService.delete(currentUser.userId(), addressId);
        return ResponseEntity.ok(ApiResponse.success("배송지가 삭제되었습니다.", null));
    }
}

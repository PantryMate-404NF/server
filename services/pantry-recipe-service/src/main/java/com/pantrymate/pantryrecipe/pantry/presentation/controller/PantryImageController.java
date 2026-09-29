package com.pantrymate.pantryrecipe.pantry.presentation.controller;

import com.pantrymate.common.dto.ApiResponse;
import com.pantrymate.pantryrecipe.pantry.application.PantryImageService;
import com.pantrymate.pantryrecipe.pantry.presentation.dto.PantryImageUploadResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "PANTRY", description = "팬트리 식재료 관리")
@RestController
@RequestMapping("/api/pantry-items/images")
public class PantryImageController {

    private final PantryImageService pantryImageService;

    public PantryImageController(PantryImageService pantryImageService) {
        this.pantryImageService = pantryImageService;
    }

    @Operation(
            summary = "팬트리 이미지 업로드",
            description = "JPEG·PNG·WebP, 5MB 이하 이미지 1장을 업로드하고 URL을 반환한다. "
                    + "반환된 imageUrl을 POST/PATCH /api/pantry-items의 imageUrl에 그대로 넣는다. "
                    + "이 URL은 이 서버 인스턴스에 저장되므로 재배포 시 사라질 수 있다(임시 저장소).")
    @SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "업로드 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400", description = "PANTRY-INVALID-IMAGE — 형식 또는 5MB 초과"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUTH-UNAUTHORIZED")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PantryImageUploadResponseDto>> upload(
            @RequestPart(value = "file", required = false) MultipartFile file) {
        PantryImageUploadResponseDto response = pantryImageService.upload(file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("이미지 업로드가 완료되었습니다.", response));
    }

    @Operation(summary = "팬트리 이미지 조회", description = "업로드 응답의 imageUrl이 가리키는 실제 이미지 파일(인증 불필요, <img> 태그용).")
    @GetMapping("/{filename}")
    public ResponseEntity<byte[]> get(@PathVariable String filename) {
        byte[] image = pantryImageService.read(filename);
        return ResponseEntity.ok().contentType(pantryImageService.contentTypeOf(filename)).body(image);
    }
}

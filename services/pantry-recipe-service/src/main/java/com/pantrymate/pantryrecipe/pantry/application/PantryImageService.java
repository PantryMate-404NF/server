package com.pantrymate.pantryrecipe.pantry.application;

import com.pantrymate.common.exception.BusinessException;
import com.pantrymate.pantryrecipe.pantry.domain.exception.PantryErrorCode;
import com.pantrymate.pantryrecipe.pantry.presentation.dto.PantryImageUploadResponseDto;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 팬트리 등록용 이미지 업로드.
 *
 * <p>지금은 이 인스턴스의 로컬 디스크에 저장한다. k8s에서 여러 pod로 뜨거나 재배포되면 파일이 사라지므로,
 * 정식 배포 전에는 S3 등 객체 스토리지로 교체해야 한다(응답 형태는 그대로 유지 가능).
 */
@Service
public class PantryImageService {

    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;
    private static final Map<String, String> ALLOWED_CONTENT_TYPES =
            Map.of("image/jpeg", "jpg", "image/png", "png", "image/webp", "webp");
    private static final Pattern SAFE_FILENAME = Pattern.compile("^[a-f0-9-]{36}\\.(jpg|png|webp)$");

    private final Path uploadDir;
    private final String baseUrl;

    public PantryImageService(
            @Value("${pantry.image.upload-dir}") String uploadDir, @Value("${pantry.image.base-url}") String baseUrl) {
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public PantryImageUploadResponseDto upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(PantryErrorCode.PANTRY_INVALID_IMAGE);
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new BusinessException(PantryErrorCode.PANTRY_INVALID_IMAGE);
        }
        String extension = ALLOWED_CONTENT_TYPES.get(file.getContentType());
        if (extension == null) {
            throw new BusinessException(PantryErrorCode.PANTRY_INVALID_IMAGE);
        }

        String filename = UUID.randomUUID() + "." + extension;
        try {
            Files.createDirectories(uploadDir);
            file.transferTo(uploadDir.resolve(filename));
        } catch (IOException e) {
            throw new BusinessException(PantryErrorCode.PANTRY_INVALID_IMAGE);
        }
        return new PantryImageUploadResponseDto(baseUrl + "/api/pantry-items/images/" + filename);
    }

    /** 업로드 때 만든 이름(UUID.확장자)만 허용한다 — 경로 조작 방지. */
    public byte[] read(String filename) {
        if (!SAFE_FILENAME.matcher(filename).matches()) {
            throw new BusinessException(PantryErrorCode.PANTRY_NOTFOUND_ITEM);
        }
        Path path = uploadDir.resolve(filename).normalize();
        if (!path.startsWith(uploadDir) || !Files.isRegularFile(path)) {
            throw new BusinessException(PantryErrorCode.PANTRY_NOTFOUND_ITEM);
        }
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new BusinessException(PantryErrorCode.PANTRY_NOTFOUND_ITEM);
        }
    }

    public MediaType contentTypeOf(String filename) {
        if (filename.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        if (filename.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        }
        return MediaType.IMAGE_JPEG;
    }
}

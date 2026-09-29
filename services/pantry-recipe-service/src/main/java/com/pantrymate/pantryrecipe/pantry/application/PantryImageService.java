package com.pantrymate.pantryrecipe.pantry.application;

import com.pantrymate.common.exception.BusinessException;
import com.pantrymate.pantryrecipe.pantry.domain.exception.PantryErrorCode;
import com.pantrymate.pantryrecipe.pantry.presentation.dto.PantryImageUploadResponseDto;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * 팬트리 등록용 이미지 업로드(S3).
 *
 * <p>버킷 내 이 prefix만 인프라 쪽에서 Public Read로 열어주기로 했다(2026-09-29, isiou).
 * 고정 URL로 바로 접근 가능해 presigned URL은 쓰지 않는다.
 *
 * <p>자격증명은 코드에서 지정하지 않는다. {@link S3Client}의 기본 자격증명 체인이 로컬에서는
 * AWS_ACCESS_KEY_ID/AWS_SECRET_ACCESS_KEY 환경변수를, EKS에서는 Pod Identity를 알아서 찾는다.
 */
@Service
public class PantryImageService {

    private static final Logger log = LoggerFactory.getLogger(PantryImageService.class);
    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;
    private static final Map<String, String> ALLOWED_CONTENT_TYPES =
            Map.of("image/jpeg", "jpg", "image/png", "png", "image/webp", "webp");

    private final S3Client s3Client;
    private final String bucket;
    private final String prefix;
    private final String region;

    public PantryImageService(
            S3Client s3Client,
            @Value("${pantry.image.s3.bucket}") String bucket,
            @Value("${pantry.image.s3.prefix}") String prefix,
            @Value("${pantry.image.s3.region}") String region) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.prefix = prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix;
        this.region = region;
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

        String key = prefix + "/" + UUID.randomUUID() + "." + extension;
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType())
                            .contentLength(file.getSize())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException | S3Exception e) {
            log.error("S3 이미지 업로드 실패 key={}: {}", key, e.getMessage());
            throw new BusinessException(PantryErrorCode.PANTRY_INVALID_IMAGE);
        }
        return new PantryImageUploadResponseDto(
                "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key);
    }
}

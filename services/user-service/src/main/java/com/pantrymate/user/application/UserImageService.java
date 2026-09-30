package com.pantrymate.user.application;

import com.pantrymate.common.exception.BusinessException;
import com.pantrymate.user.domain.exception.UserErrorCode;
import com.pantrymate.user.presentation.dto.UserProfileImageUploadResponseDto;
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

@Service
public class UserImageService {

    private static final Logger log = LoggerFactory.getLogger(UserImageService.class);
    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;
    private static final Map<String, String> ALLOWED_CONTENT_TYPES =
            Map.of("image/jpeg", "jpg", "image/png", "png", "image/webp", "webp");

    private final S3Client s3Client;
    private final String bucket;
    private final String prefix;
    private final String region;

    public UserImageService(
            S3Client s3Client,
            @Value("${user.image.s3.bucket}") String bucket,
            @Value("${user.image.s3.prefix}") String prefix,
            @Value("${user.image.s3.region}") String region) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.prefix = prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix;
        this.region = region;
    }

    public UserProfileImageUploadResponseDto upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(UserErrorCode.USER_INVALID_IMAGE);
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new BusinessException(UserErrorCode.USER_INVALID_IMAGE);
        }
        String extension = ALLOWED_CONTENT_TYPES.get(file.getContentType());
        if (extension == null) {
            throw new BusinessException(UserErrorCode.USER_INVALID_IMAGE);
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
            throw new BusinessException(UserErrorCode.USER_INVALID_IMAGE);
        }
        return new UserProfileImageUploadResponseDto("https://" + bucket + ".s3." + region + ".amazonaws.com/" + key);
    }
}

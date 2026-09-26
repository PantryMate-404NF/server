package com.pantrymate.pantryrecipe.pantry.application;

import com.pantrymate.common.exception.BusinessException;
import com.pantrymate.pantryrecipe.ingredient.domain.IngredientRepository;
import com.pantrymate.pantryrecipe.pantry.domain.PantryItem;
import com.pantrymate.pantryrecipe.pantry.domain.ReceiptOcrGateway;
import com.pantrymate.pantryrecipe.pantry.domain.ReceiptOcrGateway.OcrItem;
import com.pantrymate.pantryrecipe.pantry.domain.ReceiptOcrGateway.OcrResult;
import com.pantrymate.pantryrecipe.pantry.domain.exception.PantryErrorCode;
import com.pantrymate.pantryrecipe.pantry.presentation.dto.ReceiptOcrResponseDto;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ReceiptOcrService {

    private static final long MAX_IMAGE_BYTES = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp", "image/heic", "image/heif");
    private static final Pattern RECEIPT_ID = Pattern.compile("^[A-Za-z0-9_-]{1,64}$");

    private final ReceiptOcrGateway receiptOcrGateway;
    private final IngredientRepository ingredientRepository;

    public ReceiptOcrService(ReceiptOcrGateway receiptOcrGateway, IngredientRepository ingredientRepository) {
        this.receiptOcrGateway = receiptOcrGateway;
        this.ingredientRepository = ingredientRepository;
    }

    /** 결과는 등록 후보다. 저장은 사용자가 확인·수정한 뒤 기존 팬트리 등록 API로 한다. */
    public ReceiptOcrResponseDto recognize(String receiptId, String requestId, MultipartFile file) {
        validate(receiptId, file);
        byte[] image;
        try {
            image = file.getBytes();
        } catch (IOException e) {
            throw new BusinessException(PantryErrorCode.PANTRY_INVALID_RECEIPT);
        }

        String filename = file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()
                ? "receipt"
                : file.getOriginalFilename();
        OcrResult result = receiptOcrGateway.recognize(
                receiptId,
                requestId == null || requestId.isBlank() ? UUID.randomUUID().toString() : requestId,
                filename,
                file.getContentType(),
                image);

        List<ReceiptOcrResponseDto.Item> items = result.items().stream()
                .map(item -> new ReceiptOcrResponseDto.Item(item.name(), resolveIngredientId(item)))
                .toList();
        return ReceiptOcrResponseDto.of(result, items);
    }

    private Long resolveIngredientId(OcrItem item) {
        if (item.ingredientId() != null || item.name() == null) {
            return item.ingredientId();
        }
        return ingredientRepository
                .findByNameIgnoreCase(item.name().trim())
                .map(ingredient -> ingredient.getIngredientId())
                .orElse(null);
    }

    private void validate(String receiptId, MultipartFile file) {
        if (receiptId == null
                || !RECEIPT_ID.matcher(receiptId).matches()
                || file == null
                || file.isEmpty()
                || file.getSize() > MAX_IMAGE_BYTES
                || file.getContentType() == null
                || !ALLOWED_CONTENT_TYPES.contains(file.getContentType().toLowerCase())) {
            throw new BusinessException(PantryErrorCode.PANTRY_INVALID_RECEIPT);
        }
    }
}

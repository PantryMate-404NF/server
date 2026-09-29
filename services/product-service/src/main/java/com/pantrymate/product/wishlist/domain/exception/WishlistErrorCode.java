package com.pantrymate.product.wishlist.domain.exception;

import com.pantrymate.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum WishlistErrorCode implements ErrorCode {
    WISHLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "WISHLIST-001", "존재하지 않는 찜목록입니다.");



    private final HttpStatus status;
    private final String code;
    private final String message;
}

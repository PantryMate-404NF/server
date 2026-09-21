package com.pantrymate.pantryrecipe.recipe.domain.exception;

import com.pantrymate.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum RecipeErrorCode implements ErrorCode {
    RECIPE_NOTFOUND_ID(HttpStatus.NOT_FOUND, "RECIPE-NOTFOUND-ID", "해당 레시피를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    RecipeErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}

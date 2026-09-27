package com.pantrymate.pantryrecipe.recipe.domain.exception;

import com.pantrymate.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum RecipeErrorCode implements ErrorCode {
    RECIPE_NOTFOUND_ID(HttpStatus.NOT_FOUND, "RECIPE-NOTFOUND-ID", "해당 레시피를 찾을 수 없습니다."),
    RECIPE_INVALID_FILTER(HttpStatus.BAD_REQUEST, "RECIPE-INVALID-FILTER", "필터 식재료는 최대 3개까지 선택할 수 있습니다."),
    RECIPE_UNAVAILABLE_RECOMMEND(
            HttpStatus.SERVICE_UNAVAILABLE, "RECIPE-UNAVAILABLE-RECOMMEND", "추천 정보를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요."),
    RECIPE_INVALID_SEARCH_KEYWORD(HttpStatus.BAD_REQUEST, "RECIPE-INVALID-SEARCH-KEYWORD", "검색어를 입력해주세요.");

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

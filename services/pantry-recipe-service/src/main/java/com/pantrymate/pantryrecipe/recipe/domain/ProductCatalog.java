package com.pantrymate.pantryrecipe.recipe.domain;

import java.util.List;

public interface ProductCatalog {

    /** 식재료가 매핑된 판매중 상품 후보. 상품 서비스 장애 시 마지막 성공 결과(없으면 빈 목록)를 반환한다. */
    List<ProductCandidate> getOnSaleCandidates();
}

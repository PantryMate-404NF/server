package com.pantrymate.product.category.presentation.controller;

import com.pantrymate.common.dto.ApiResponse;
import com.pantrymate.product.category.application.dto.CategoryListResponse;
import com.pantrymate.product.category.application.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ApiResponse<CategoryListResponse> getCategoryList() {
        CategoryListResponse response = categoryService.getCategoryList();
        return ApiResponse.success("카테고리 목록을 조회했습니다.", response);
    }
}


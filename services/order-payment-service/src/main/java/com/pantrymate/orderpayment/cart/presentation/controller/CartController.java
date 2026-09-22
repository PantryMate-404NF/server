package com.pantrymate.orderpayment.cart.presentation.controller;

import com.pantrymate.common.dto.ApiResponse;
import com.pantrymate.common.dto.CurrentUser;
import com.pantrymate.orderpayment.cart.application.dto.CartItemAddRequest;
import com.pantrymate.orderpayment.cart.application.dto.CartItemAddResponse;
import com.pantrymate.orderpayment.cart.application.dto.CartItemResponse;
import com.pantrymate.orderpayment.cart.application.dto.CartItemUpdateRequest;
import com.pantrymate.orderpayment.cart.application.dto.CartResponse;
import com.pantrymate.orderpayment.cart.application.service.CartService;
import com.pantrymate.orderpayment.cart.domain.CartItems;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CartItemAddResponse> addItem(
        CurrentUser currentUser,
        @Valid @RequestBody CartItemAddRequest request
    ){
        CartItems items = cartService.addItem(currentUser.userId(),request);
        CartItemAddResponse response =  CartItemAddResponse.from(items);
        return ApiResponse.success("정상적으로 장바구니에 상품이 추가되었습니다.", response);
    }

    @GetMapping
    public ApiResponse<CartResponse> getCart(
        CurrentUser currentUser
    ){
       CartResponse response = cartService.getCart(currentUser.userId());
       return ApiResponse.success("정상적으로 장바구니가 조회되었습니다.", response);
    }

    @PatchMapping("/items/{cartItemId}")
    public ApiResponse<CartItemAddResponse> updateCart(
        CurrentUser currentUser,
        @PathVariable Long cartItemId,
        @Valid @RequestBody CartItemUpdateRequest request
    ){
        CartItems items = cartService.updateCart(currentUser.userId(), cartItemId, request);
        CartItemAddResponse response =  CartItemAddResponse.from(items);

        return ApiResponse.success("정상적으로 수량 조정이 완료되었습니다.", response);
    }

    @DeleteMapping("/items/{cartItemId}")
    public ApiResponse<Void> deleteItem(
        CurrentUser currentUser,
        @PathVariable Long cartItemId
    ) {
        cartService.deleteCart(currentUser.userId(), cartItemId);
        return ApiResponse.success("장바구니 상품이 정상적으로 제거되었습니다.");
    }

}

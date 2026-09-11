package com.pantrymate.orderpayment.order.presentation.controller;

import com.pantrymate.common.dto.ApiResponse;
import com.pantrymate.common.dto.CurrentUser;
import com.pantrymate.orderpayment.order.application.dto.OrderCreateRequest;
import com.pantrymate.orderpayment.order.application.dto.OrderCreateResponse;
import com.pantrymate.orderpayment.order.application.service.OrderService;
import com.pantrymate.orderpayment.order.domain.Orders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrderCreateResponse> createOrder(CurrentUser currentUser,
        @RequestHeader("Idempotency-Key") String idempotencyKey,
        @RequestBody OrderCreateRequest request) {
        Orders order = orderService.createOrder(currentUser.userId(), request, idempotencyKey);
        OrderCreateResponse response = OrderCreateResponse.from(order);
        return ApiResponse.success("정상적으로 주문서 생성이 완료되었습니다.", response);
    }

}

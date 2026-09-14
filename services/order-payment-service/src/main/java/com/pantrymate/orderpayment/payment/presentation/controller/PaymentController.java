package com.pantrymate.orderpayment.payment.presentation.controller;

import com.pantrymate.common.dto.ApiResponse;
import com.pantrymate.common.dto.CurrentUser;
import com.pantrymate.orderpayment.payment.application.dto.PaymentConfirmRequest;
import com.pantrymate.orderpayment.payment.application.dto.PaymentDetailResponse;
import com.pantrymate.orderpayment.payment.application.dto.PaymentPrepareResponse;
import com.pantrymate.orderpayment.payment.application.dto.PaymentConfirmResponse;
import com.pantrymate.orderpayment.payment.application.service.PaymentService;
import com.pantrymate.orderpayment.payment.domain.Payments;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{orderId}/prepare")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PaymentPrepareResponse> preparePayment(
        CurrentUser currentUser,
        @PathVariable String orderId
    ) {
        Payments payment = paymentService.preparePayment(currentUser.userId(), orderId);
        return ApiResponse.success("결제가 준비되었습니다.", PaymentPrepareResponse.from(payment));
    }

    @PostMapping("/confirm")
    public ApiResponse<PaymentConfirmResponse> confirmPayment(
        CurrentUser currentUser,
        @Valid @RequestBody PaymentConfirmRequest request
    ) {
        Payments payment = paymentService.confirmPayment(currentUser.userId(), request);
        return ApiResponse.success("결제가 승인되었습니다.", PaymentConfirmResponse.from(payment));
    }

    @GetMapping("/{orderId}")
    public ApiResponse<PaymentDetailResponse> getPayment(
        CurrentUser currentUser,
        @PathVariable String orderId
    ){
        Payments payment = paymentService.getPayments(currentUser.userId(), orderId);
        return ApiResponse.success("정상적으로 결제 조회가 완료되었습니다.", PaymentDetailResponse.from(payment));
    }
}
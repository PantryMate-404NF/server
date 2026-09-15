package com.pantrymate.orderpayment.payment.infrastructure.client;

import com.pantrymate.orderpayment.payment.application.dto.TossCancelRequest;
import com.pantrymate.orderpayment.payment.application.dto.TossCancelResponse;
import com.pantrymate.orderpayment.payment.application.dto.TossConfirmRequest;
import com.pantrymate.orderpayment.payment.application.dto.TossConfirmResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "toss-payment", url = "https://api.tosspayments.com/v1")
public interface TossPaymentClient {

    @PostMapping("/payments/confirm")
    TossConfirmResponse confirmToss(
        @RequestHeader("Authorization") String authorization,
        @RequestBody TossConfirmRequest request
    );

    @PostMapping("/payments/{paymentKey}/cancel")
    TossCancelResponse cancelToss(
        @RequestHeader("Authorization") String authorization,
        @PathVariable("paymentKey") String paymentKey,
        @RequestBody TossCancelRequest request
    );

}


package com.pantrymate.orderpayment.payment.application.service;

import com.pantrymate.common.exception.BusinessException;
import com.pantrymate.orderpayment.order.domain.Orders;
import com.pantrymate.orderpayment.order.domain.exception.OrderErrorCode;
import com.pantrymate.orderpayment.order.domain.repository.OrderRepository;
import com.pantrymate.orderpayment.payment.application.dto.PaymentConfirmRequest;
import com.pantrymate.orderpayment.payment.application.dto.TossConfirmRequest;
import com.pantrymate.orderpayment.payment.application.dto.TossConfirmResponse;
import com.pantrymate.orderpayment.payment.domain.Payments;
import com.pantrymate.orderpayment.payment.domain.enums.PaymentStatus;
import com.pantrymate.orderpayment.payment.domain.exception.PaymentErrorCode;
import com.pantrymate.orderpayment.payment.domain.repository.PaymentRepository;
import com.pantrymate.orderpayment.payment.infrastructure.client.TossAuthorizationEncoding;
import com.pantrymate.orderpayment.payment.infrastructure.client.TossPaymentClient;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RedissonClient redissonClient;
    private final OrderRepository orderRepository;
    private final TossAuthorizationEncoding tossAuthorizationEncoding;
    private final TossPaymentClient tossPaymentClient;

    @Transactional
    public Payments confirmPayment(Long userId, PaymentConfirmRequest request) {
        RLock lock = redissonClient.getLock("payment_lock" + request.orderId());

        boolean acquired = false;
        try {
            acquired = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!acquired) {
                throw new BusinessException(PaymentErrorCode.PAYMENT_IN_PROGRESS);
            }
            Orders order = orderRepository.findByOrderId(request.orderId())
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
            if (!order.getUserId().equals(userId)) {
                throw new BusinessException(OrderErrorCode.ORDER_NOT_FOUND);
            }
            if (!order.getTotalAmount().equals(request.amount())) {
                throw new BusinessException(PaymentErrorCode.AMOUNT_MISMATCH);
            }
            String authHeader = tossAuthorizationEncoding.createAuthorization();
            TossConfirmRequest tossConfirmRequest = new TossConfirmRequest(request.paymentKey(),
                request.orderId(), request.amount());
            TossConfirmResponse response = tossPaymentClient.confirmToss(authHeader,
                tossConfirmRequest);
            Payments payment = paymentRepository.findByOrderIdAndStatusIn(
                    order.getId(), List.of(PaymentStatus.READY, PaymentStatus.IN_PROGRESS))
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));
            payment.approve(response.paymentKey(), response.method(), response.toString());
            order.confirm();
            return paymentRepository.save(payment);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(PaymentErrorCode.TOSS_API_ERROR);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
    @Transactional
    public Payments preparePayment(Long userId, String orderId) {
        Orders order = orderRepository.findByOrderId(orderId)
            .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(OrderErrorCode.ORDER_NOT_FOUND);
        }

        Payments payment = Payments.createReady(order.getId(), order.getTotalAmount());
        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public Payments getPayments(Long userId, String orderId) {
        Orders order = orderRepository.findByOrderId(orderId)
            .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
        if(!order.getUserId().equals(userId)) {
            throw new BusinessException(OrderErrorCode.ORDER_NOT_FOUND);
        }
        return paymentRepository.findByOrderId(order.getId())
            .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));

    }

}

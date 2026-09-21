package com.pantrymate.orderpayment.payment.application.service;

import com.pantrymate.common.exception.BusinessException;
import com.pantrymate.orderpayment.order.domain.OrderItems;
import com.pantrymate.orderpayment.order.domain.Orders;
import com.pantrymate.orderpayment.order.domain.exception.OrderErrorCode;
import com.pantrymate.orderpayment.order.domain.repository.OrderItemRepository;
import com.pantrymate.orderpayment.order.domain.repository.OrderRepository;
import com.pantrymate.orderpayment.payment.application.dto.PaymentConfirmRequest;
import com.pantrymate.orderpayment.payment.application.dto.PaymentDetailResponse;
import com.pantrymate.orderpayment.payment.application.dto.PaymentPrepareResponse;
import com.pantrymate.orderpayment.payment.application.dto.StockDeductionItem;
import com.pantrymate.orderpayment.payment.application.dto.StockDeductionRequest;
import com.pantrymate.orderpayment.payment.application.dto.StockRestoreRequest;
import com.pantrymate.orderpayment.payment.application.dto.TossCancelRequest;
import com.pantrymate.orderpayment.payment.application.dto.TossConfirmRequest;
import com.pantrymate.orderpayment.payment.application.dto.TossConfirmResponse;
import com.pantrymate.orderpayment.payment.domain.Payments;
import com.pantrymate.orderpayment.payment.domain.enums.PaymentStatus;
import com.pantrymate.orderpayment.payment.domain.exception.PaymentErrorCode;
import com.pantrymate.orderpayment.payment.domain.repository.PaymentRepository;
import com.pantrymate.orderpayment.payment.infrastructure.client.TossAuthorizationEncoding;
import com.pantrymate.orderpayment.payment.infrastructure.client.TossPaymentClient;
import com.pantrymate.orderpayment.product.client.ProductServiceClient;
import feign.FeignException;
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
    private final OrderItemRepository orderItemRepository;
    private final TossAuthorizationEncoding tossAuthorizationEncoding;
    private final TossPaymentClient tossPaymentClient;
    private final ProductServiceClient productServiceClient;

    @Transactional
    public Payments confirmPayment(Long userId, PaymentConfirmRequest request) {
        RLock lock = redissonClient.getLock("payment_lock" + request.orderId());

        boolean acquired = false;
        try {
            acquired = lock.tryLock(3, 30, TimeUnit.SECONDS);
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
            Payments payment = paymentRepository.findByOrderIdAndStatusIn(
                    order.getId(), List.of(PaymentStatus.READY, PaymentStatus.IN_PROGRESS))
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));
            TossConfirmResponse response;
            try {
                response = tossPaymentClient.confirmToss(authHeader, tossConfirmRequest);
            } catch (FeignException.BadRequest e) {
                payment.fail("FAILED", e.getMessage(), e.contentUTF8());
                order.fail();
                paymentRepository.save(payment);
                throw new BusinessException(PaymentErrorCode.PAYMENT_FAILED);
            }
            payment.approve(response.paymentKey(), response.method(), response.toString());
            List<OrderItems> orderItems = orderItemRepository.findByOrderId(order.getId());
            List<StockDeductionItem> stockItems = toStockDeductionItems(orderItems);
            StockDeductionRequest stockRequest = new StockDeductionRequest(stockItems);

            try {
                productServiceClient.decreaseStocks(stockRequest);
            }catch (FeignException e){
                log.error("재고 차감 실패 - status: {}, message: {}, body: {}",
                    e.status(), e.getMessage(), e.contentUTF8());
                String cancelHeader = tossAuthorizationEncoding.createAuthorization();
                TossCancelRequest cancelRequest = new TossCancelRequest("재고부족으로 인한 환불");
                tossPaymentClient.cancelToss(cancelHeader, payment.getPaymentKey(), cancelRequest);
                payment.cancel();
                order.fail();
                paymentRepository.save(payment);
                throw new BusinessException(PaymentErrorCode.INSUFFICIENT_STOCK);
            }
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
    public PaymentPrepareResponse preparePayment(Long userId, String orderId) {
        Orders order = orderRepository.findByOrderId(orderId)
            .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(OrderErrorCode.ORDER_NOT_FOUND);
        }

        Payments payment = Payments.createReady(order.getId(), order.getTotalAmount());
        paymentRepository.save(payment);
        return PaymentPrepareResponse.of(payment, order);
    }

    @Transactional(readOnly = true)
    public PaymentDetailResponse getPayments(Long userId, String orderId) {
        Orders order = orderRepository.findByOrderId(orderId)
            .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(OrderErrorCode.ORDER_NOT_FOUND);
        }
        Payments payment  = paymentRepository.findByOrderId(order.getId())
            .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));
        return PaymentDetailResponse.of(payment, order);

    }

    @Transactional
    public PaymentDetailResponse cancelPayment(Long userId, String orderId, String cancelReason) {
        Orders order = orderRepository.findByOrderId(orderId)
            .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(OrderErrorCode.ORDER_NOT_FOUND);
        }
        order.requestCancel();

        Payments payment = paymentRepository.findByOrderId(order.getId())
            .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));
        String authHeader = tossAuthorizationEncoding.createAuthorization();
        TossCancelRequest request = new TossCancelRequest(cancelReason);
        tossPaymentClient.cancelToss(authHeader, payment.getPaymentKey(), request);
        payment.cancel();
        paymentRepository.save(payment);

        List<OrderItems> orderItems = orderItemRepository.findByOrderId(order.getId());
        List<StockDeductionItem> stockItem = toStockDeductionItems(orderItems);
        StockRestoreRequest restoreRequest = new StockRestoreRequest(stockItem);
        productServiceClient.increaseStocks(restoreRequest);
        order.completeCancel();
        return PaymentDetailResponse.of(payment, order);
    }

    private List<StockDeductionItem> toStockDeductionItems(List<OrderItems> orderItems) {
        return orderItems.stream()
            .map(orderItem -> new StockDeductionItem(orderItem.getProductId(),
                orderItem.getQuantity()))
            .toList();
    }





}

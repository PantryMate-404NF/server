package com.pantrymate.orderpayment.payment.domain.repository;

import com.pantrymate.orderpayment.payment.domain.Payments;
import com.pantrymate.orderpayment.payment.domain.enums.PaymentStatus;
import java.util.List;
import java.util.Optional;


public interface PaymentRepository {
    Payments save(Payments payments);

    Optional<Payments> findById(Long id);

    Optional<Payments> findByOrderIdAndStatusIn(Long orderId, List<PaymentStatus> statuses);

    Optional<Payments> findByPaymentKey(String paymentKey);

    Optional<Payments> findByOrderId(Long orderId);



}

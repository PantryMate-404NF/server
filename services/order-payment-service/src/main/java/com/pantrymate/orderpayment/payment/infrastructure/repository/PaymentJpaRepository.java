package com.pantrymate.orderpayment.payment.infrastructure.repository;

import com.pantrymate.orderpayment.payment.domain.Payments;
import com.pantrymate.orderpayment.payment.domain.enums.PaymentStatus;
import com.pantrymate.orderpayment.payment.domain.repository.PaymentRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<Payments, Long>, PaymentRepository {
    @Override
    Optional<Payments> findById(Long id);

    @Override
    Optional<Payments> findByPaymentKey(String paymentKey);

    @Override
    Optional<Payments> findByOrderIdAndStatusIn(Long orderId, List<PaymentStatus> statuses);

    @Override
    Optional<Payments> findByOrderId(Long orderId);

}

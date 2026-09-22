package com.pantrymate.orderpayment.payment.domain;

import com.pantrymate.orderpayment.payment.domain.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Table(name = "payments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Payments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private String paymentKey;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long totalAmount;

    private String method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;


    private String failCode;
    private String failReason;

    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime cancelledAt;

    @Column(columnDefinition = "TEXT")
    private String rawResponse;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static Payments createReady(Long orderId, Long totalAmount) {
        return Payments.builder()
            .orderId(orderId)
            .totalAmount(totalAmount)
            .status(PaymentStatus.READY)
            .requestedAt(LocalDateTime.now())
            .build();
    }

    public void approve(String paymentKey, String method, String rawResponse) {
        if (status != PaymentStatus.READY && status != PaymentStatus.IN_PROGRESS) {
            throw new IllegalStateException("결제 가능한 상태가 아닙니다.");
        }
        this.paymentKey = paymentKey;
        this.method = method;
        this.status = PaymentStatus.DONE;
        this.rawResponse = rawResponse;
        this.approvedAt = LocalDateTime.now();
    }

    public void fail(String failCode, String failReason, String rawResponse) {
        this.status = PaymentStatus.ABORTED;
        this.failCode = failCode;
        this.failReason = failReason;
        this.rawResponse = rawResponse;
    }

    public void cancel(){
        if(status != PaymentStatus.DONE){
            throw new IllegalStateException("결제 완료 상태에서만 취소를 진행할 수 있습니다.");
        }
        this.status = PaymentStatus.CANCELED;
        this.cancelledAt = LocalDateTime.now();
    }


}

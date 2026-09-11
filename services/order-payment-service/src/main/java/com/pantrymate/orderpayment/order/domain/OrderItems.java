package com.pantrymate.orderpayment.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_items")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderItems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private Integer quantity;

    public static OrderItems create(Long orderId, Long productId, String productName, Long price, Integer quantity) {
        return OrderItems.builder()
            .orderId(orderId)
            .productId(productId)
            .productName(productName)
            .price(price)
            .quantity(quantity)
            .build();
    }

}

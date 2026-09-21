package com.pantrymate.orderpayment.cart.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart_items", uniqueConstraints = {
    @UniqueConstraint(name = "uk_cart_items_cart_id_product_id",
    columnNames = {"cart_id", "product_id"})
})
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItems {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long cartId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    public static CartItems create(Long cartId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }
        return CartItems.builder()
            .cartId(cartId)
            .productId(productId)
            .quantity(quantity)
            .build();
    }


    public void changeQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1개 이상이여야합니다.");
        }
        this.quantity = quantity;
    }

    public void addQuantity(Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1개 이상이여야합니다.");
        }
        //오버플로우 관리를 위해 Math.addExact 함수 사용
        this.quantity += Math.addExact(this.quantity, quantity);
    }
}

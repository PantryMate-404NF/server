package com.pantrymate.product.wishlist.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "wishlists",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_wishlists_user_product", columnNames = {"user_id", "product_id"})
    },
    indexes = {
        @Index(name = "ix_wishlists_user_created", columnList = "user_id, created_at")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Wishlists {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long productId;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;


    private Wishlists(Long userId, Long productId) {
        this.userId = userId;
        this.productId = productId;
    }
    public static Wishlists create(Long userId, Long productId) {
        return new Wishlists(userId, productId);
    }
}

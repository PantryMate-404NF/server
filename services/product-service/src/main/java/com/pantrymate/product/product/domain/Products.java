package com.pantrymate.product.product.domain;

import com.pantrymate.product.product.domain.enums.ProductStatus;
import com.pantrymate.product.product.domain.enums.ProductUnit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상품 엔티티.
 *
 * [MSA 설계 원칙] category_id는 같은 product-service 안이라 FK 가능.
 * ingredient_id는 Pantry 도메인(별도 서비스) 소유의 표준 식재료를 가리키는
 * 논리적 참조값이다. FK 제약은 걸지 않으며, 매핑 여부에 따라 null일 수 있다.
 *
 * [팀 컨벤션] @AllArgsConstructor로 생성자가 열려있지만,
 * new Products(...)는 애플리케이션 계층(ProductService)에서만 호출한다.
 *
 * [상태 관리] status는 재고 변화와 연동된다. stockQuantity가 0이 되면
 * decreaseStock()이 자동으로 OUT_OF_STOCK으로 전이시키고, 재입고되어
 * 수량이 다시 늘어나면 restock()이 ON_SALE로 복귀시킨다.
 * DISCONTINUED 이후 완전히 접는 결정은 status가 아니라 deletedAt(소프트 삭제)으로
 * 표현하며, 물리적 삭제는 하지 않는다 (이력 보존 원칙).
 */
@Entity
@Table(name = "products", uniqueConstraints = {
    @UniqueConstraint(columnNames = "sku")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Products {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long categoryId;

    @Column(nullable = false)
    private Long price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductUnit unit;

    /** 중량/용량 실측치 (예: 500). 단위는 unit 참고 */
    private Integer capacity;

    /** 구성 개수 (예: 3개입 → 3) */
    private Integer packageCount;

    /** 원산지 */
    private String origin;

    /** 상품 상세 설명 */
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String thumbnailUrl;

    /** 팬트리 표준 식재료 참조값. 매핑 전에는 null일 수 있다. */
    private Long ingredientId;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    @Version
    private Long version;

    /**
     * 상태 전이 규칙(ProductStatus.canTransitionTo)을 검증한 뒤에만 상태를 바꾼다.
     */
    private void changeStatus(ProductStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                String.format("%s 상태에서 %s로 변경할 수 없습니다.", this.status, newStatus)
            );
        }
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    /** 재고를 차감한다. 차감 후 재고가 0이 되면 자동으로 품절 상태로 전이한다. */
    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("차감 수량은 0보다 커야 합니다.");
        }
        if (this.stockQuantity < quantity) {
            throw new IllegalStateException("재고가 부족합니다. 현재 재고: " + this.stockQuantity);
        }
        this.stockQuantity -= quantity;
        this.updatedAt = LocalDateTime.now();

        if (this.stockQuantity == 0 && this.status == ProductStatus.ON_SALE) {
            changeStatus(ProductStatus.OUT_OF_STOCK);
        }
    }

    /** 재입고 처리. 품절 상태였다면 판매중으로 복귀시킨다. */
    public void restock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("재입고 수량은 0보다 커야 합니다.");
        }
        this.stockQuantity += quantity;
        this.updatedAt = LocalDateTime.now();

        if (this.status == ProductStatus.OUT_OF_STOCK) {
            changeStatus(ProductStatus.ON_SALE);
        }
    }

    /** 판매 중단 처리 (재개 가능성 있는 상태). */
    public void discontinue() {
        changeStatus(ProductStatus.DISCONTINUED);
    }

    /**
     * 소프트 삭제. DISCONTINUED 상태를 거친 상품만 삭제할 수 있다
     * (판매중/품절 상태에서 곧바로 삭제하는 것은 막는다).
     */
    public void delete() {
        if (this.status != ProductStatus.DISCONTINUED) {
            throw new IllegalStateException("판매중단(DISCONTINUED) 상태의 상품만 삭제할 수 있습니다.");
        }
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /** 팬트리 표준 식재료와의 매핑 여부. */
    public boolean isMappedToIngredient() {
        return this.ingredientId != null;
    }
}

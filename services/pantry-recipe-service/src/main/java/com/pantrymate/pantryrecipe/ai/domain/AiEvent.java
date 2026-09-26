package com.pantrymate.pantryrecipe.ai.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** AI 서버로 보낼 행동 이벤트(outbox). 사용자 행동과 같은 트랜잭션에서 쌓고 스케줄러가 전송한다. */
@Entity
@Table(name = "ai_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiEvent {

    public static final String PENDING = "PENDING";
    public static final String SENT = "SENT";
    public static final String DEAD = "DEAD";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "event_type", nullable = false, length = 20)
    private String eventType;

    @Column(name = "recipe_id", nullable = false)
    private Long recipeId;

    @Column(name = "request_id", length = 64)
    private String requestId;

    private Integer position;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    @Column(nullable = false, length = 10)
    private String status;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    private AiEvent(Long userId, String eventType, Long recipeId, String requestId, Integer position) {
        this.userId = userId;
        this.eventType = eventType;
        this.recipeId = recipeId;
        this.requestId = requestId;
        this.position = position;
        this.occurredAt = OffsetDateTime.now();
        this.status = PENDING;
    }

    public static AiEvent create(Long userId, String eventType, Long recipeId, String requestId, Integer position) {
        return new AiEvent(userId, eventType, recipeId, requestId, position);
    }

    public void markSent() {
        this.status = SENT;
    }

    /** 실패 횟수를 올리고 maxAttempts에 도달하면 더 보내지 않는다. */
    public void markFailed(int maxAttempts) {
        this.attempts++;
        if (this.attempts >= maxAttempts) {
            this.status = DEAD;
        }
    }

    public void markDead() {
        this.status = DEAD;
    }
}

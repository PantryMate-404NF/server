package com.pantrymate.pantryrecipe.ai.domain;

import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AiEventRepository extends JpaRepository<AiEvent, Long> {

    /** 여러 인스턴스가 떠 있어도 같은 이벤트를 두 번 보내지 않도록 잠근 채 가져온다. */
    @Query(
            value = "SELECT * FROM ai_events WHERE status = 'PENDING' ORDER BY event_id LIMIT :limit FOR UPDATE SKIP LOCKED",
            nativeQuery = true)
    List<AiEvent> lockPending(@Param("limit") int limit);

    @Modifying
    @Query("DELETE FROM AiEvent e WHERE e.status <> 'PENDING' AND e.createdAt < :before")
    int deleteFinishedBefore(@Param("before") OffsetDateTime before);
}

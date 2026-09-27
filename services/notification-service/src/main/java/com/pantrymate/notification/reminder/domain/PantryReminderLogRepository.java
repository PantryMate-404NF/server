package com.pantrymate.notification.reminder.domain;

import java.time.OffsetDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PantryReminderLogRepository extends JpaRepository<PantryReminderLog, Long> {

    boolean existsByUserIdAndSentAtAfter(Long userId, OffsetDateTime after);
}

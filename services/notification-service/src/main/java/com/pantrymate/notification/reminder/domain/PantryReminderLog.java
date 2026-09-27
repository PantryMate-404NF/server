package com.pantrymate.notification.reminder.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pantry_reminder_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PantryReminderLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pantry_reminder_log_id")
    private Long pantryReminderLogId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "sent_at", nullable = false)
    private OffsetDateTime sentAt;

    @PrePersist
    void prePersist() {
        this.sentAt = OffsetDateTime.now();
    }

    public static PantryReminderLog create(Long userId) {
        PantryReminderLog log = new PantryReminderLog();
        log.userId = userId;
        return log;
    }
}

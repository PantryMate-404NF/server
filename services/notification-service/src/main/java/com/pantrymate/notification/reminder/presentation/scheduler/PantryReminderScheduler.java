package com.pantrymate.notification.reminder.presentation.scheduler;

import com.pantrymate.notification.reminder.application.PantryReminderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PantryReminderScheduler {

    private final PantryReminderService pantryReminderService;

    public PantryReminderScheduler(PantryReminderService pantryReminderService) {
        this.pantryReminderService = pantryReminderService;
    }

    @Scheduled(cron = "0 0 19 * * MON", zone = "Asia/Seoul")
    public void remindPantry() {
        pantryReminderService.sendWeeklyReminders();
    }
}

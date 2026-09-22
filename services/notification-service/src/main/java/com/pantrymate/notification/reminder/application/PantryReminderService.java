package com.pantrymate.notification.reminder.application;

import com.pantrymate.notification.devicetoken.domain.DeviceToken;
import com.pantrymate.notification.devicetoken.domain.DeviceTokenRepository;
import com.pantrymate.notification.reminder.domain.PantryReminderLog;
import com.pantrymate.notification.reminder.domain.PantryReminderLogRepository;
import com.pantrymate.notification.reminder.domain.PushMessage;
import com.pantrymate.notification.reminder.domain.PushSender;
import com.pantrymate.notification.reminder.infrastructure.PantryRecipeClient;
import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PantryReminderService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private static final PushMessage REMINDER_MESSAGE = new PushMessage(
            "PANTRY_REMINDER", "팬트리 확인 시간이에요!", "식재료 상태를 확인하고 필요한 정보를 업데이트 해주세요", "/pantry");

    private final PantryRecipeClient pantryRecipeClient;
    private final DeviceTokenRepository deviceTokenRepository;
    private final PantryReminderLogRepository pantryReminderLogRepository;
    private final PushSender pushSender;

    public PantryReminderService(
            PantryRecipeClient pantryRecipeClient,
            DeviceTokenRepository deviceTokenRepository,
            PantryReminderLogRepository pantryReminderLogRepository,
            PushSender pushSender) {
        this.pantryRecipeClient = pantryRecipeClient;
        this.deviceTokenRepository = deviceTokenRepository;
        this.pantryReminderLogRepository = pantryReminderLogRepository;
        this.pushSender = pushSender;
    }

    public void sendWeeklyReminders() {
        List<Long> userIds = pantryRecipeClient.listPantryItemUserIds().data();
        if (userIds == null || userIds.isEmpty()) {
            log.info("팬트리 리마인드 대상 없음");
            return;
        }

        OffsetDateTime weekStart = OffsetDateTime.now(ZONE)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .toLocalDate()
                .atStartOfDay(ZONE)
                .toOffsetDateTime();

        int sent = 0;
        int failed = 0;
        for (Long userId : userIds) {
            try {
                if (sendTo(userId, weekStart)) {
                    sent++;
                }
            } catch (RuntimeException e) {
                failed++;
                log.warn("팬트리 리마인드 발송 실패 userId={}", userId, e);
            }
        }
        log.info("팬트리 리마인드 완료 대상={} 발송={} 실패={}", userIds.size(), sent, failed);
    }

    private boolean sendTo(Long userId, OffsetDateTime weekStart) {
        if (pantryReminderLogRepository.existsByUserIdAndSentAtAfter(userId, weekStart)) {
            return false;
        }
        Optional<DeviceToken> deviceToken = deviceTokenRepository.findByUserId(userId);
        if (deviceToken.isEmpty()) {
            return false;
        }
        pushSender.send(deviceToken.get().getFcmToken(), REMINDER_MESSAGE);
        pantryReminderLogRepository.save(PantryReminderLog.create(userId));
        return true;
    }
}

package com.pantrymate.pantryrecipe.ai.application;

import com.pantrymate.pantryrecipe.ai.domain.AiEvent;
import com.pantrymate.pantryrecipe.ai.domain.AiEventRepository;
import com.pantrymate.pantryrecipe.ai.domain.AiEventSink;
import com.pantrymate.pantryrecipe.ai.domain.AiEventSink.Result;
import java.time.OffsetDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class AiEventRelayService {

    private static final Logger log = LoggerFactory.getLogger(AiEventRelayService.class);
    private static final int BATCH_SIZE = 200;
    private static final int MAX_BATCHES_PER_RUN = 10;
    private static final int MAX_ATTEMPTS = 5;
    private static final int RETENTION_DAYS = 7;

    private final AiEventRepository aiEventRepository;
    private final AiEventSink sink;
    private final TransactionTemplate transactionTemplate;
    private final boolean enabled;

    public AiEventRelayService(
            AiEventRepository aiEventRepository,
            AiEventSink sink,
            TransactionTemplate transactionTemplate,
            @Value("${ai.events.enabled}") boolean enabled) {
        this.aiEventRepository = aiEventRepository;
        this.sink = sink;
        this.transactionTemplate = transactionTemplate;
        this.enabled = enabled;
    }

    @Scheduled(fixedDelayString = "${ai.events.interval-ms}")
    public void flush() {
        if (!enabled) {
            return;
        }
        try {
            for (int i = 0; i < MAX_BATCHES_PER_RUN; i++) {
                Boolean full = transactionTemplate.execute(status -> sendBatch());
                if (!Boolean.TRUE.equals(full)) {
                    break;
                }
            }
            transactionTemplate.executeWithoutResult(
                    status -> aiEventRepository.deleteFinishedBefore(OffsetDateTime.now().minusDays(RETENTION_DAYS)));
        } catch (RuntimeException e) {
            log.warn("AI 이벤트 전송 중 오류: {}", e.getMessage());
        }
    }

    /** 한 배치를 보내고, 다음 배치를 더 보낼 만큼 가득 찼으면 true. */
    private boolean sendBatch() {
        List<AiEvent> batch = aiEventRepository.lockPending(BATCH_SIZE);
        if (batch.isEmpty()) {
            return false;
        }
        Result result = sink.send(batch);
        switch (result) {
            case OK -> batch.forEach(AiEvent::markSent);
            case RETRYABLE -> {
                batch.forEach(event -> event.markFailed(MAX_ATTEMPTS));
                return false;
            }
            case REJECTED -> {
                if (batch.size() == 1) {
                    log.error("AI가 이벤트를 거부해 더 보내지 않는다 eventId={}", batch.get(0).getEventId());
                    batch.get(0).markDead();
                } else {
                    // 묶음 중 일부가 문제일 수 있으므로 하나씩 보내 문제 이벤트만 버린다.
                    sendOneByOne(batch);
                }
            }
        }
        return batch.size() == BATCH_SIZE;
    }

    private void sendOneByOne(List<AiEvent> batch) {
        for (AiEvent event : batch) {
            Result result = sink.send(List.of(event));
            switch (result) {
                case OK -> event.markSent();
                case RETRYABLE -> event.markFailed(MAX_ATTEMPTS);
                case REJECTED -> {
                    log.error("AI가 이벤트를 거부해 더 보내지 않는다 eventId={}", event.getEventId());
                    event.markDead();
                }
            }
        }
    }
}

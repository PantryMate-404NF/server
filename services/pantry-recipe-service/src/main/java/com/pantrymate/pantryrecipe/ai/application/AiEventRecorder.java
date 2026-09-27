package com.pantrymate.pantryrecipe.ai.application;

import com.pantrymate.pantryrecipe.ai.domain.AiEvent;
import com.pantrymate.pantryrecipe.ai.domain.AiEventRepository;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** 호출한 쪽 트랜잭션에 참여해 이벤트를 쌓는다. 이벤트 값이 이상해도 사용자 행동은 실패시키지 않는다. */
@Service
public class AiEventRecorder {

    public static final String CLICK = "click";
    public static final String SAVE = "save";
    public static final String UNSAVE = "unsave";
    public static final String COOK = "cook";

    private static final Pattern REQUEST_ID = Pattern.compile("^[A-Za-z0-9-]{1,64}$");

    private final AiEventRepository aiEventRepository;
    private final boolean enabled;

    public AiEventRecorder(AiEventRepository aiEventRepository, @Value("${ai.events.enabled}") boolean enabled) {
        this.aiEventRepository = aiEventRepository;
        this.enabled = enabled;
    }

    public void record(Long userId, String eventType, Long recipeId, String requestId, Integer position) {
        if (!enabled || userId == null) {
            return;
        }
        String validRequestId = requestId != null && REQUEST_ID.matcher(requestId).matches() ? requestId : null;
        Integer validPosition = position != null && position >= 1 ? position : null;
        aiEventRepository.save(AiEvent.create(userId, eventType, recipeId, validRequestId, validPosition));
    }
}

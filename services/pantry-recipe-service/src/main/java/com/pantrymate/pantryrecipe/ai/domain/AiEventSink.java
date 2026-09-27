package com.pantrymate.pantryrecipe.ai.domain;

import java.util.List;

public interface AiEventSink {

    enum Result {
        /** 전달 완료 */
        OK,
        /** 네트워크 오류·5xx·인증 오류 등 나중에 다시 시도하면 되는 실패 */
        RETRYABLE,
        /** 요청 내용이 거부됨(4xx) — 같은 내용은 다시 보내도 실패한다 */
        REJECTED
    }

    Result send(List<AiEvent> events);
}

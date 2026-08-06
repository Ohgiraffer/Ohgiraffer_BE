package com.ohgiraffer.global.sse;

/*
* comment.
*  타 도메인(알림, 결재 등)이 실시간 이벤트를 발행할 때 의존하는 인터페이스
*  구현체(SseEmitterManager)를 직접 알 필요 없이 이것만 주입받아 publish 호출하면 됨
*  나중에 Redis Pub/Sub 기반으로 바꿀 때도 이 인터페이스를 유지한 채 구현체만 교체하면 됨
* */

public interface SseEventPublisher {

    /**
     * 특정 유저에게 이벤트를 발행.
     *
     * @param userId    수신 대상 유저 ID
     * @param eventName 이벤트 이름 (프론트에서 EventSource.addEventListener로 구분)
     * @param data      전송할 데이터 (알림 DTO 등)
     */
    void publish(Long userId, String eventName, Object data);

}

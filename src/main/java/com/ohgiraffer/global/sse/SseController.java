package com.ohgiraffer.global.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/*
* comment.
*  SSE 구독 엔드포인트
*  클라이언트가 이 API를 EventSource로 열면 실시간 알림을 수신하기 시작함
* */

@RestController
@RequiredArgsConstructor
public class SseController {

    private final SseEmitterManager sseEmitterManager;

    /**
     * SSE 구독 시작
     * X-Accel-Buffering: no 헤더는 Nginx 등 프록시가 응답을 버퍼링하지 않고 즉시 스트리밍하도록 강제하기 위함
     * — 없으면 로컬에선 멀쩡한데 배포 환경(프록시 있음)에서만 이벤트가 지연/누락될 수 있음
     */
    @GetMapping(value = "/notifications/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe(/* @AuthenticationPrincipal 등 인증 정보로 userId 추출 */) {
        Long userId = null; // TODO: 실제 인증 붙으면 여기서 꺼내기
        SseEmitter emitter = sseEmitterManager.connect(userId);

        return ResponseEntity.ok()
                .header("X-Accel-Buffering", "no")
                .body(emitter);
    }

}

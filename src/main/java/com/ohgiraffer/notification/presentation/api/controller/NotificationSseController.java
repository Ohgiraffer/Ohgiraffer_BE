package com.ohgiraffer.notification.presentation.api.controller;

import com.ohgiraffer.global.sse.SseEmitterManager;
import com.ohgiraffer.global.sse.SseTicketService;
import com.ohgiraffer.notification.presentation.api.response.SseTicketResponse;
import com.ohgiraffer.security.user.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/*
 * comment.
 *  실시간 알림 구독 전용 컨트롤러
 *  - 티켓 발급(POST /subscribe/ticket): 기존 JWT 인증(Authorization 헤더) 그대로 통과한 요청만 호출 가능
 *  - 구독 연결(GET /subscribe): EventSource가 커스텀 헤더를 못 보내는 스펙 제약 때문에
 *    SecurityConfig에서 permitAll로 열어두고, 대신 쿼리 파라미터 ticket을 이 컨트롤러 안에서 직접 검증함
 *  - 연결 유지 및 이벤트 push 자체는 기존과 동일하게 SseEmitterManager가 전담
 */

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationSseController {

    private final SseEmitterManager sseEmitterManager;
    private final SseTicketService sseTicketService;

    /**
     * SSE 구독용 단기 티켓 발급
     * 프론트가 EventSource를 열기 직전에 기존 Authorization 헤더 방식으로 호출함
     * 응답으로 받은 ticket은 30초 내에, 1회만 구독 연결에 사용 가능
     */
    @PostMapping("/subscribe/ticket")
    public ResponseEntity<SseTicketResponse> issueTicket(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        String ticket = sseTicketService.issue(principal.getId());
        return ResponseEntity.ok(new SseTicketResponse(ticket));
    }

    /**
     * SSE 구독 연결
     * 이 경로는 SecurityConfig에서 permitAll 처리되어 있어 JwtAuthenticationFilter를 거치지 않음
     * 대신 쿼리 파라미터로 받은 ticket을 SseTicketService로 직접 검증해서 userId를 얻음
     * 티켓이 없거나 만료/사용됐으면 401 반환
     */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe(@RequestParam String ticket) {
        Long userId = sseTicketService.validateAndConsume(ticket);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        SseEmitter emitter = sseEmitterManager.connect(userId);

        return ResponseEntity.ok()
                // SSE 응답이 중간 프록시/CDN에 캐싱되지 않도록 명시
                .header("Cache-Control", "no-store")
                // Nginx 등 프록시가 응답을 버퍼링하지 않고 즉시 스트리밍하도록 강제
                .header("X-Accel-Buffering", "no")
                .body(emitter);
    }

}

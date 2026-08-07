package com.ohgiraffer.notification.presentation.api.controller;

import com.ohgiraffer.global.sse.SseEmitterManager;
import com.ohgiraffer.security.user.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/*
 * comment.
 *  실시간 알림 구독 전용 컨트롤러
 *  기존 SseEmitterManager(글로벌 SSE 인프라)에 연결만 위임, 알림 도메인 고유 로직 없음
 */

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationSseController {

    private final SseEmitterManager sseEmitterManager;

    // SSE 구독 연결 - 연결 유지 및 이벤트 push는 SseEmitterManager가 전담
    @GetMapping("/subscribe")
    public SseEmitter subscribe(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return sseEmitterManager.connect(principal.getId());
    }

}

package com.ohgiraffer.chatbot.presentation.api.controller;

import com.ohgiraffer.chatbot.application.helper.ChatbotOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/* comment.
 *  Sendbird Bot 전용 콜백 엔드포인트 (콜백 라우팅 B안)
 *  - 기존 ChatWebhookService(그룹 채널 웹훅)와 완전히 분리된 엔드포인트
 *  - 200 OK 필수(안 주면 Sendbird가 재시도) - 예외 발생해도 일단 200 반환하고 내부 로그만 남기는 방어적 처리
 */

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatbotWebhookController {

    private final ChatbotOrchestrator chatbotOrchestrator;

    @PostMapping("/webhooks/sendbird/bot")
    public ResponseEntity<Void> handleBotCallback(@RequestBody String payload) {
        log.info("[ChatbotWebhook] 원본 payload={}", payload);
        try {
            chatbotOrchestrator.handleIncomingMessage(payload);
        } catch (Exception e) {
            // Sendbird 재시도 폭주 방지 - 실패해도 200으로 응답하고 로그로만 추적
            log.error("[ChatbotWebhook] 처리 중 예외 발생 | payload={}", payload, e);
        }
        return ResponseEntity.ok().build();
    }

}

package com.ohgiraffer.chatbot.presentation.api.controller;

import com.ohgiraffer.chat.application.port.SendbirdApiPort;
import com.ohgiraffer.chatbot.application.helper.ChatbotOrchestrator;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/*
 * comment.
 *   Sendbird Bot 전용 콜백 엔드포인트 (콜백 라우팅 B안)
 *  - 기존 ChatWebhookService(그룹 채널 웹훅)와 완전히 분리된 엔드포인트
 *  - 200 OK 필수(안 주면 Sendbird가 재시도) - 예외 발생해도 일단 200 반환하고 내부 로그만 남기는 방어적 처리
 *  - 단, 서명 검증 실패는 예외로 그대로 전파해 401 반환 (재시도 폭주 방지 로직과는 별개 -
 *    위조된 요청을 200으로 받아주면 안 됨. ChatWebhookController와 동일한 SendbirdApiPort.verifyWebhookSignature() 재사용)
 */

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatbotWebhookController {

    private final ChatbotOrchestrator chatbotOrchestrator;
    private final SendbirdApiPort sendbirdApiPort;

    @PostMapping("/webhooks/sendbird/bot")
    public ResponseEntity<Void> handleBotCallback(
            @RequestBody String payload,
            @RequestHeader("x-sendbird-signature") String signature
    ) {
        if (!sendbirdApiPort.verifyWebhookSignature(payload, signature)) {
            throw new BusinessException(ErrorCode.CHAT_WEBHOOK_SIGNATURE_INVALID);
        }

        // 유저 발화 원문/식별정보가 포함될 수 있어 운영 환경에서는 비활성화되는 DEBUG로만 남김
        log.debug("[ChatbotWebhook] 원본 payload={}", payload);
        try {
            chatbotOrchestrator.handleIncomingMessage(payload);
        } catch (Exception e) {
            // Sendbird 재시도 폭주 방지 - 실패해도 200으로 응답하고 로그로만 추적
            // payload 원문은 개인정보 우려로 ERROR 레벨에 남기지 않음
            log.error("[ChatbotWebhook] 처리 중 예외 발생", e);
        }
        return ResponseEntity.ok().build();
    }

}

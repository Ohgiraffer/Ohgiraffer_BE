package com.ohgiraffer.chat.presentation.api.controller;

import com.ohgiraffer.chat.application.port.SendbirdApiPort;
import com.ohgiraffer.chat.application.service.ChatWebhookService;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/*
 * comment.
 *  Sendbird 웹훅 수신 엔드포인트
 *  서명 검증은 컨트롤러에서 즉시 처리, 실제 파싱/미러링 반영은 ChatWebhookService에 위임
 */

@RestController
@RequestMapping("/webhooks/sendbird")
@RequiredArgsConstructor
public class ChatWebhookController {

    private final SendbirdApiPort sendbirdApiPort;
    private final ChatWebhookService chatWebhookService;

    @PostMapping
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("x-sendbird-signature") String signature
    ) {
        if (!sendbirdApiPort.verifyWebhookSignature(payload, signature)) {
            throw new BusinessException(ErrorCode.CHAT_WEBHOOK_SIGNATURE_INVALID);
        }

        chatWebhookService.handle(payload);

        return ResponseEntity.ok().build();
    }

}

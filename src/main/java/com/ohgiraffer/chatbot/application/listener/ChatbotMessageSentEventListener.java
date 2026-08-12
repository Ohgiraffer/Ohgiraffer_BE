package com.ohgiraffer.chatbot.application.listener;

import com.ohgiraffer.chat.domain.event.ChatMessageSentEvent;
import com.ohgiraffer.chatbot.application.helper.ChatbotOrchestrator;
import com.ohgiraffer.chatbot.application.port.ChatbotChannelPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/*
 * comment.
 *  ChatMessageSentEvent 구독자
 *  - chat 도메인 트랜잭션 커밋 완료 후에만 동작함 (AFTER_COMMIT)
 *  - Sendbird 웹훅(bot_callback_url) 미수신 문제로 인한 대체 경로:
 *    이벤트로 받은 channelId가 AI비서 채널이면 챗봇 대화 로직을 직접 트리거함
 *  - 여기서 예외가 발생해도 이미 커밋된 메시지 전송 트랜잭션에는 영향 없음.
 *    재시도 큐 없이 로그만 남기고 종료함 (사용자는 챗봇 무응답을 그대로 겪을 수 있음 - 확인된 설계임)
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatbotMessageSentEventListener {

    private final ChatbotChannelPort chatbotChannelPort;
    private final ChatbotOrchestrator chatbotOrchestrator;

    // 커밋 이후에만 실행 - 메시지 저장 트랜잭션과 완전히 분리됨
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChatMessageSent(ChatMessageSentEvent event) {
        // AI비서 채널이 아니면 조용히 무시
        if (!chatbotChannelPort.isChatbotChannel(event.channelId())) {
            return;
        }

        try {
            chatbotOrchestrator.handleDirectMessage(event.senderId(), event.channelId(), event.content());
        } catch (Exception e) {
            // 재시도 없음 - 로그만 남기고 종료 (설계상 확인된 사항)
            log.error("[Chatbot] 웹훅 우회 경로 - 직접 트리거 실패 | channelId={}, senderId={}",
                    event.channelId(), event.senderId(), e);
        }
    }

}

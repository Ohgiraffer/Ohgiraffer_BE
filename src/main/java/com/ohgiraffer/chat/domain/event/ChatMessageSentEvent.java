package com.ohgiraffer.chat.domain.event;

/*
 * comment.
 *  채팅 메시지 전송 완료 이벤트
 *  - ChatMessageCommandService.sendMessage() 성공(커밋) 후 발행됨
 *  - chat 도메인은 이 이벤트를 누가 구독하는지 알지 못함 (단방향 의존 - chatbot 쪽에서 구독)
 */

public record ChatMessageSentEvent(
        String channelId,   // Sendbird 채널 URL
        Long senderId,      // 메시지 발신자 userId
        String content      // 메시지 본문
) {
}

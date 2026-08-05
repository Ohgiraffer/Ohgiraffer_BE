package com.ohgiraffer.chat.presentation.api.response;

import com.ohgiraffer.chat.application.result.SendbirdMessageResult;

import java.time.Instant;

/*
 * comment.
 *  메시지/답글 전송 응답
 */

public record ChatMessageResponse(
        String sendbirdMessageId,
        String channelId,
        Long senderId,
        String content,
        String attachmentUrl,
        String messageType,
        Instant sentAt
) {

    public static ChatMessageResponse from(SendbirdMessageResult result) {
        return new ChatMessageResponse(
                result.sendbirdMessageId(), result.channelId(), result.senderId(),
                result.content(), result.attachmentUrl(), result.messageType(), result.sentAt()
        );
    }

    // ChatMessageResponse에 추가 - ChatMessageResult(조회용) 기준 변환
    public static ChatMessageResponse from(com.ohgiraffer.chat.application.result.ChatMessageResult result) {
        return new ChatMessageResponse(
                result.sendbirdMessageId(), result.channelId(), result.senderId(),
                result.content(), result.attachmentUrl(), null, result.sentAt()
        );
    }

}

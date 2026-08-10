package com.ohgiraffer.chat.presentation.api.response;

import com.ohgiraffer.chat.application.result.ChatMessageResult;
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
        Instant sentAt,
        Long unreadCount
) {

    // 메시지 전송 직후 응답 - 안읽음 수 개념이 없는 시점이라 항상 null
    public static ChatMessageResponse from(SendbirdMessageResult result) {
        return new ChatMessageResponse(
                result.sendbirdMessageId(), result.channelId(), result.senderId(),
                result.content(), result.attachmentUrl(), result.messageType(), result.sentAt(), null
        );
    }

    // ChatMessageResult(조회용) 기준 변환
    public static ChatMessageResponse from(ChatMessageResult result) {
        return new ChatMessageResponse(
                result.sendbirdMessageId(), result.channelId(), result.senderId(),
                result.content(), result.attachmentUrl(),
                result.attachmentUrl() != null ? "FILE" : "MESG", result.sentAt(), result.unreadCount()
        );
    }

}

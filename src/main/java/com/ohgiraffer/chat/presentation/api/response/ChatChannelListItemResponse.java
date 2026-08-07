package com.ohgiraffer.chat.presentation.api.response;

/*
 * comment.
 *  참여 채팅방 목록 응답 항목
 */

import com.ohgiraffer.chat.application.result.ChatChannelListItemResult;

import java.time.Instant;

public record ChatChannelListItemResponse(
        String channelId,
        String name,
        String channelType,
        String lastMessageContent,
        Instant lastMessageSentAt,
        long unreadCount,
        Boolean isOnline
) {

    // ChatChannelListItemResult(application 계층 결과)를 presentation 응답으로 변환
    public static ChatChannelListItemResponse from(ChatChannelListItemResult result) {
        return new ChatChannelListItemResponse(
                result.channelId(), result.name(), result.channelType(), result.lastMessageContent(),
                result.lastMessageSentAt(), result.unreadCount(), result.isOnline()
        );
    }

}

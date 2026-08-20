package com.ohgiraffer.chat.application.result;

/*
 * comment.
 *  참여 채팅방 목록 항목
 */

import java.time.Instant;

public record ChatChannelListItemResult(
        String channelId,
        String name,
        String channelType,
        String lastMessageContent,
        Instant lastMessageSentAt,
        long unreadCount,
        String profileImageUrl,
        Boolean isOnline
) {
}

package com.ohgiraffer.chat.application.command;

import java.util.List;

/*
 * comment.
 *  메시지 전송 커맨드
 */

public record SendMessageCommand(
        String channelId,
        Long senderId,
        String content,
        String attachmentUrl,
        List<Long> mentionedUserIds
) {
}

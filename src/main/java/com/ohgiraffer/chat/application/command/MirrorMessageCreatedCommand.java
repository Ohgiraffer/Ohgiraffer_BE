package com.ohgiraffer.chat.application.command;

import java.time.LocalDateTime;

/*
 * comment.
 *  Sendbird 웹훅 - 메시지/답글 전송 이벤트 미러링 커맨드
 *  parentMessageId가 있으면 스레드 답글, 없으면 일반 메시지
 */

public record MirrorMessageCreatedCommand(
        String channelId,
        String sendbirdMessageId,
        Long parentMessageId,
        Long senderId,
        String content,
        String attachmentUrl,
        String attachmentType,
        LocalDateTime sentAt
) {
}

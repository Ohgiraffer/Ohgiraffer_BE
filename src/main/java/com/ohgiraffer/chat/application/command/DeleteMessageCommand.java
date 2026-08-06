package com.ohgiraffer.chat.application.command;

/*
 * comment.
 *  메시지/답글 삭제 커맨드
 */

public record DeleteMessageCommand(
        String channelId,
        String sendbirdMessageId,
        Long requesterId
) {
}

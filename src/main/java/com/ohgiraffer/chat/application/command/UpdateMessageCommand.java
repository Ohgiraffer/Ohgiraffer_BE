package com.ohgiraffer.chat.application.command;

/*
 * comment.
 *  메시지/답글 수정 커맨드
 *  답글도 chat_message_mirror의 동일 레코드이므로 메시지/답글 구분 없이 하나의 커맨드로 처리
 */

public record UpdateMessageCommand(
        String channelId,
        String sendbirdMessageId,
        Long requesterId,
        String content
) {
}

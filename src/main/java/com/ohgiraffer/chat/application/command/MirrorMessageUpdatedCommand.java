package com.ohgiraffer.chat.application.command;

/*
 * comment.
 *  Sendbird 웹훅 - 메시지/답글 수정 이벤트 미러링 커맨드
 */

public record MirrorMessageUpdatedCommand(
        String sendbirdMessageId,
        String content
) {
}

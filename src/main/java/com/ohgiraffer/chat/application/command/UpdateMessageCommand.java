package com.ohgiraffer.chat.application.command;

/*
 * comment.
 *  메시지/답글 수정 커맨드
 *  답글도 chat_message_mirror의 동일 레코드이므로 메시지/답글 구분 없이 하나의 커맨드로 처리
 *  attachmentUrl: null이면 기존 첨부파일 유지, 빈 값/무효값이면 첨부파일 제거
 */

public record UpdateMessageCommand(
        String channelId,
        String sendbirdMessageId,
        Long requesterId,
        String content,
        String attachmentUrl
) {
}

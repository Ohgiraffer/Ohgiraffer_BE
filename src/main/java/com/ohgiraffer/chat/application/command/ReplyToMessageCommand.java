package com.ohgiraffer.chat.application.command;

/*
 * comment.
 *  스레드 답글 작성 커맨드
 *  parentMessageId로 원본 메시지를 참조, 실제 Sendbird 반영은 SendbirdApiPort.sendReply가 처리
 *  content 없이 attachmentUrl만으로도 답글 가능 (텍스트+첨부 동시 가능)
 */

public record ReplyToMessageCommand(
        String channelId,
        Long parentMessageId,
        Long senderId,
        String content,
        String attachmentUrl

) {
}

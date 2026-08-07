package com.ohgiraffer.chat.application.command;

/*
 * comment.
 *  스레드 답글 작성 커맨드
 *  parentMessageId로 원본 메시지를 참조, 실제 Sendbird 반영은 SendbirdApiPort.sendReply가 처리
 *  content 없이 attachmentUrl만으로도 답글 가능 (텍스트+첨부 동시 가능)
 *  parentSendbirdMessageId는 클라이언트가 응답에서 받은 Sendbird 메시지 ID(String) 그대로 전달받음
 *  실제 Sendbird 반영/미러링 저장 시 필요한 값(Sendbird 숫자ID, 우리 내부 PK)으로의 변환은 ChatReplyCommandService가 담당

 */

public record ReplyToMessageCommand(
        String channelId,
        String parentSendbirdMessageId,
        Long senderId,
        String content,
        String attachmentUrl

) {
}

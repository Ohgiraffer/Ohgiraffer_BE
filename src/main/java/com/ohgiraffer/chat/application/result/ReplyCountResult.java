package com.ohgiraffer.chat.application.result;

/*
 * comment.
 *  특정 메시지에 달린 답글 개수 조회 결과
 */

public record ReplyCountResult(
        String messageId,
        long replyCount
) {
}

package com.ohgiraffer.chat.presentation.api.request;

/*
 * comment.
 *  스레드 답글 작성 요청
 *  URL이 /chat/messages/{messageId}/replies라 channelId는 경로에 없어서 body에 포함시킴
 */

public record ReplyRequest(
        String channelId,
        String content,
        String attachmentUrl
) {
}

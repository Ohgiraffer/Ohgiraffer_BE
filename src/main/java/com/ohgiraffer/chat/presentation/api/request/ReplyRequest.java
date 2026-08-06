package com.ohgiraffer.chat.presentation.api.request;

/*
 * comment.
 *  스레드 답글 작성 요청
 *  URL이 /chat/messages/{messageId}/replies라 channelId는 경로에 없어서 body에 포함시킴
 */

import jakarta.validation.constraints.NotBlank;

public record ReplyRequest(
        @NotBlank(message = "channelId는 필수입니다.")
        String channelId,
        String content,
        String attachmentUrl
) {
}

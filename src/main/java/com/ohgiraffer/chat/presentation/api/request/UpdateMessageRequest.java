package com.ohgiraffer.chat.presentation.api.request;

/*
 * comment.
 *  메시지/답글 수정 요청
 */

public record UpdateMessageRequest(
        String channelId,
        String content
) {
}

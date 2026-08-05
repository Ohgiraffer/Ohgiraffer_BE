package com.ohgiraffer.chat.presentation.api.request;

import java.util.List;

/*
 * comment.
 *  메시지 전송 요청
 */

public record SendMessageRequest(
        String content,
        String attachmentUrl,
        List<Long> mentionedUserIds
) {
}

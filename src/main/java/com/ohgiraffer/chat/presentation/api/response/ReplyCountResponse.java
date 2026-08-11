package com.ohgiraffer.chat.presentation.api.response;

public record ReplyCountResponse(
        String messageId,
        long replyCount
) {
}

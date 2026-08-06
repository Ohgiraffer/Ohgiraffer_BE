package com.ohgiraffer.chat.presentation.api.request;

/*
 * comment.
 *  메시지/답글 수정 요청
 */

import jakarta.validation.constraints.NotBlank;

public record UpdateMessageRequest(
        @NotBlank(message = "channelId는 필수입니다.")
        String channelId,

        @NotBlank(message = "수정할 내용은 공백일 수 없습니다.")
        String content
) {
}

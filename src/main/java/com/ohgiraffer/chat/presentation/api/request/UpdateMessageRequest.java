package com.ohgiraffer.chat.presentation.api.request;

/*
 * comment.
 *  메시지/답글 수정 요청
 *  attachmentUrl: 미전달(null) 시 첨부파일 유지, 명시적으로 빈 값 보내면 첨부파일 제거(FILE->MESG 전환)
 */

import jakarta.validation.constraints.NotBlank;

public record UpdateMessageRequest(
        @NotBlank(message = "channelId는 필수입니다.")
        String channelId,

        String content,

        String attachmentUrl
) {
}

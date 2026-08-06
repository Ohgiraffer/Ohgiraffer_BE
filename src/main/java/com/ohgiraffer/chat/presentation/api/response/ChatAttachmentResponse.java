package com.ohgiraffer.chat.presentation.api.response;

/*
 * comment.
 *  채팅 첨부파일 업로드 응답
 *  url을 그대로 메시지 전송 API(POST /chat/channels/{channelId}/messages)의
 *  attachmentUrl 필드에 넣어서 재호출하는 흐름으로 사용됨
 */

public record ChatAttachmentResponse(
        String url
) {
}

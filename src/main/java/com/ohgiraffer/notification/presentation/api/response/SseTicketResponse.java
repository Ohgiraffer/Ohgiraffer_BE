package com.ohgiraffer.notification.presentation.api.response;

/*
 * comment.
 *  SSE 티켓 발급 API(POST /notifications/subscribe/ticket) 응답 DTO
 *  ticket 값은 30초 후 만료되는 1회용 값 - 프론트는 발급 즉시 EventSource 연결에 사용해야 함
 */

public record SseTicketResponse(
        String ticket
) {
}

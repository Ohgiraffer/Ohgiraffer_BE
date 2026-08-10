package com.ohgiraffer.chat.presentation.api.response;

/*
 * comment.
 *  헤더 상시 노출용 - 전체 채널 안읽은 메시지 합계 응답
 */

public record ChatUnreadCountResponse(
        long totalUnreadCount
) {
}

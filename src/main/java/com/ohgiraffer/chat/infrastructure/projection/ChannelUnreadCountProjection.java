package com.ohgiraffer.chat.infrastructure.projection;

/*
 * comment.
 *  채널별 안읽음수 조회 결과 매핑용 (조인+GROUP BY 네이티브 쿼리)
 */

public interface ChannelUnreadCountProjection {

    Long getChatChannelId();
    Long getUnreadCount();

}

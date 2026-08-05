package com.ohgiraffer.chat.infrastructure.persistence;

import java.time.Instant;

/*
 * comment.
 *  채널별 최신메시지 1건 조회 결과 매핑용 (윈도우 함수 네이티브 쿼리)
 */

public interface ChannelLastMessageProjection {

    String getChannelId();
    String getContent();
    Instant getSentAt();

}

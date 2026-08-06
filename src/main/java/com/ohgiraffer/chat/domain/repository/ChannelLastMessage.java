package com.ohgiraffer.chat.domain.repository;

import java.time.Instant;

/*
 * comment.
 *  채널별 최신메시지 도메인 표현 - infrastructure 프로젝션과 분리
 */

public record ChannelLastMessage(

        String content,
        Instant sentAt

) {
}

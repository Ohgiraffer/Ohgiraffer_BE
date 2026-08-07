package com.ohgiraffer.chat.application.result;

import java.time.Instant;
import java.util.List;

/*
 * comment.
 *  그룹 채팅방 상세 조회 결과
 *  readCount/readUserIds는 채널 최신 메시지 id 기준으로 각 멤버의 lastReadMessageId를 비교해서 계산
 *  최신 메시지가 없는 채널(빈 채팅방)이면 전원 읽음 처리
 */

public record ChatChannelDetailResult(
        String channelId,
        String name,
        String channelType,
        List<ChatChannelMemberResult> members,
        int readCount,
        List<Long> readUserIds
) {

    public record ChatChannelMemberResult(
            Long userId,
            String memberName,
            String email,
            String role,
            Instant joinedAt,
            Long lastReadMessageId,
            boolean isRead
    ) {
    }

}

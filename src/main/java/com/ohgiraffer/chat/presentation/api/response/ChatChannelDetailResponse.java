package com.ohgiraffer.chat.presentation.api.response;

import com.ohgiraffer.chat.application.result.ChatChannelDetailResult;

import java.time.Instant;
import java.util.List;

/*
 * comment.
 *  그룹 채팅방 상세 조회 응답
 */

public record ChatChannelDetailResponse(
        String channelId,
        String name,
        String channelType,
        List<MemberResponse> members,
        int readCount,
        List<Long> readUserIds
) {

    public record MemberResponse(
            Long userId, String memberName, String email, String role,
            String profileImageUrl, Instant joinedAt, boolean isRead
    ) {
    }

    public static ChatChannelDetailResponse from(ChatChannelDetailResult result) {
        List<MemberResponse> members = result.members().stream()
                .map(m -> new MemberResponse(
                        m.userId(), m.memberName(), m.email(), m.role(),
                        m.profileImageUrl(), m.joinedAt(), m.isRead()
                ))
                .toList();
        return new ChatChannelDetailResponse(
                result.channelId(), result.name(), result.channelType(),
                members, result.readCount(), result.readUserIds()
        );
    }

}

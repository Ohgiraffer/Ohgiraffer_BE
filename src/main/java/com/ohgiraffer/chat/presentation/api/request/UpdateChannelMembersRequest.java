package com.ohgiraffer.chat.presentation.api.request;

import java.util.List;

/*
 * comment.
 *  팀변경 시 채널 멤버 반영 요청
 */

public record UpdateChannelMembersRequest(
        String channelId,
        List<Long> addUserIds,
        List<Long> removeUserIds
) {
}

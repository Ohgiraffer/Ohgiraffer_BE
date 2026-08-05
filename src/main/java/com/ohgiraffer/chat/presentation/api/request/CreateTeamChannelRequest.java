package com.ohgiraffer.chat.presentation.api.request;

import java.util.List;

/*
 * comment.
 *  팀 채팅방 자동 생성 요청
 */

public record CreateTeamChannelRequest(
        List<Long> memberUserIds
) {
}

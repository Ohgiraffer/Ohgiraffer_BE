package com.ohgiraffer.chat.application.command;

import java.util.List;

/*
 * comment.
 *  채팅방 생성 커맨드
 *  userIds 1명 -> 1:1(DM), 2명 이상 -> 그룹으로 자동 분기 (ChatChannelCommandService에서 판단)
 */

public record CreateChannelCommand(
        List<Long> userIds,
        String name
) {
}

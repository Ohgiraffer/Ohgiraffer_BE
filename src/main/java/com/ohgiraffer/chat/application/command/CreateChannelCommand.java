package com.ohgiraffer.chat.application.command;

import java.util.List;

/*
 * comment.
 *  채팅방 생성 커맨드
 *  userIds 1명 -> 1:1(DM), 2명 이상 -> 그룹으로 자동 분기 (ChatChannelCommandService에서 판단)
 *  senderId(채널 생성자) 추가 - 컨트롤러에서 principal 기반으로 채움
 */

public record CreateChannelCommand(
        Long senderId,
        List<Long> userIds,
        String name
) {
}

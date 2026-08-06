package com.ohgiraffer.chat.application.command;

import java.util.List;

/*
 * comment.
 *  팀 변경 시 채널 멤버 반영 커맨드
 *  addUserIds: 새로 합류한 팀원, removeUserIds: 제외된 팀원
 */

public record UpdateChannelMembersCommand(
        String channelId,
        List<Long> addUserIds,
        List<Long> removeUserIds
) {
}

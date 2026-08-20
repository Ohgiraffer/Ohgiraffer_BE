package com.ohgiraffer.team.application.outbox;

import java.util.List;

public record SendbirdChannelSyncPayload(
        Long teamId,
        List<Long> memberUserIds,
        boolean createChatChannel
) {
}
package com.ohgiraffer.team.application.event;

public record TeamExternalResourceDeleteTarget(
        Long teamId,
        String sendbirdChannelUrl,
        String notionPageId
) {

    public TeamExternalResourceDeleteTarget {
        if (teamId == null || teamId <= 0) {
            throw new IllegalArgumentException("teamId가 올바르지 않습니다.");
        }
    }
}
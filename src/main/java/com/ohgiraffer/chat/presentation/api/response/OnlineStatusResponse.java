package com.ohgiraffer.chat.presentation.api.response;

import com.ohgiraffer.chat.application.result.SendbirdUserStatus;

import java.time.Instant;

/*
 * comment.
 *  온라인 상태 조회 응답
 */

public record OnlineStatusResponse(
        Long userId,
        boolean isOnline,
        Instant lastSeenAt
) {

    public static OnlineStatusResponse from(SendbirdUserStatus status) {
        return new OnlineStatusResponse(status.userId(), status.isOnline(), status.lastSeenAt());
    }

}

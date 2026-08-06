package com.ohgiraffer.chat.application.result;

import java.time.Instant;

/*
 * comment.
 *  온라인 상태 조회 결과
 *  lastSeenAt은 오프라인일 때만 의미 있음 (온라인이면 null)
 */

public record SendbirdUserStatus(
        Long userId,
        boolean isOnline,
        Instant lastSeenAt
) {
}

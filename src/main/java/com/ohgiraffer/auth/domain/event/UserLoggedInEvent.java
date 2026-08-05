package com.ohgiraffer.auth.domain.event;

/*
 * comment.
 *  로그인 성공 시 발행되는 도메인 이벤트
 */

public record UserLoggedInEvent(
        Long userId,      // 로그인한 유저 ID
        String name,      // 유저 이름 (Sendbird nickname으로 사용)
        String profileImg // 프로필 이미지 URL (nullable)
) {
}

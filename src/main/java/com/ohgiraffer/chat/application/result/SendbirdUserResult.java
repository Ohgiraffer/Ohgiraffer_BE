package com.ohgiraffer.chat.application.result;

/*
 * comment.
 *  Sendbird 유저 검색 결과
 *  Sendbird API 원본 응답에서 채팅 상대 검색에 필요한 필드만 추려서 담음
 */

public record SendbirdUserResult(
        Long userId,
        String nickname,
        String profileUrl,
        boolean isOnline
) {
}

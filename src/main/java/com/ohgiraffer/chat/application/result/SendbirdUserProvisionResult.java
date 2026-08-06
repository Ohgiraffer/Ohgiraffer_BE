package com.ohgiraffer.chat.application.result;

/*
 * comment.
 *  Sendbird 유저 프로비저닝 결과
 *  accessToken은 프론트가 Sendbird SDK를 초기화할 때 사용하는 값
 *  -> 우리 JWT와는 별개 토큰, Sendbird 서버 인증 전용
 */

public record SendbirdUserProvisionResult(
        Long userId,
        String name,
        String accessToken
) {
}

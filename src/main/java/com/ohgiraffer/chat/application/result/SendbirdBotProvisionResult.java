package com.ohgiraffer.chat.application.result;

/* comment.
 *  Sendbird 봇 리소스 생성 응답 결과
 */

public record SendbirdBotProvisionResult(
        String botUserId,
        String nickname,
        String botToken
) {
}

package com.ohgiraffer.chat.presentation.api.response;

import java.time.Instant;

/*
* comment.
*  Sendbird 세션 토큰 발급 응답 DTO
*  프론트가 SendbirdChat.connect()에 그대로 넘기는 값들
* */

public record SendbirdSessionTokenResponse(
        //  프론트 connect()용 유저 ID
        String sendbirdUserId,
        // Sendbird 세션 토큰 (마스터키 아님, 노출 가능)
        String sessionToken,
        // Sendbird Application ID, 공개값
        String appId,
        // 토큰 만료 시각
        Instant expiresAt
) {
}

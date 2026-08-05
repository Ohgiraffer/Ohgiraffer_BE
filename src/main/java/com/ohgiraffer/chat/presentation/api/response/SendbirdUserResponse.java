package com.ohgiraffer.chat.presentation.api.response;

import com.ohgiraffer.chat.application.result.SendbirdUserResult;

/*
 * comment.
 *  채팅 상대 검색 결과 응답
 */

public record SendbirdUserResponse(
        Long userId,
        String name,
        String profileUrl,
        boolean isOnline
) {

    public static SendbirdUserResponse from(SendbirdUserResult result) {
        return new SendbirdUserResponse(result.userId(), result.name(), result.profileUrl(), result.isOnline());
    }

}

package com.ohgiraffer.chat.presentation.api.request;

import java.util.List;

/*
 * comment.
 *  채팅방 생성 요청
 */


public record CreateChannelRequest(
        List<Long> userIds,
        String name
) {
}

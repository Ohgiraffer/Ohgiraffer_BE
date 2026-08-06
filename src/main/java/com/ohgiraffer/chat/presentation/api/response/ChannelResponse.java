package com.ohgiraffer.chat.presentation.api.response;

import com.ohgiraffer.chat.application.result.ChatChannelResult;

/*
 * comment.
 *  채팅방 생성/조회 응답
 */

public record ChannelResponse(
        String channelId,
        String name
) {

    public static ChannelResponse from(ChatChannelResult result) {
        return new ChannelResponse(result.channelId(), result.name());
    }

}

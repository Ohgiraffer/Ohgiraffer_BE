package com.ohgiraffer.chat.presentation.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/*
 * comment.
 *  채팅방 생성 요청
 */


public record CreateChannelRequest(
        @NotEmpty(message = "참여자는 최소 1명 이상이어야 합니다.")
        List<@NotNull(message = "userId는 null일 수 없습니다.") Long> userIds,

        String name
) {
}

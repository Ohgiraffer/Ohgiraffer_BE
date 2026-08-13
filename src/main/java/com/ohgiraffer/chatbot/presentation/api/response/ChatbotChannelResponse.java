package com.ohgiraffer.chatbot.presentation.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * comment.
 *  GET /chatbot/channel 응답 DTO
 *  - 프론트가 소비하는 필드명은 channel_url(snake_case) - 전역 Jackson 네이밍 전략에 의존하지 않고
 *    명시적으로 고정해서, 전역 설정이 무엇이든 이 응답의 필드명은 항상 보장되게 함
 */

public record ChatbotChannelResponse(
        @JsonProperty("channel_url") String channelUrl
) {
}

package com.ohgiraffer.chatbot.presentation.api.controller;

import com.ohgiraffer.chatbot.application.usecase.ChatbotChannelQueryUseCase;
import com.ohgiraffer.chatbot.presentation.api.response.ChatbotChannelResponse;
import com.ohgiraffer.security.user.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * comment.
 *  AI비서 챗봇 채널 조회 API
 *  - 프론트가 로그인 후 이 API를 호출해 channel_url을 받아 Sendbird 채팅 UI를 오픈함
 */

@RestController
@RequiredArgsConstructor
public class ChatbotChannelController {

    private final ChatbotChannelQueryUseCase chatbotChannelQueryUseCase;

    // GET /chatbot/channel - 없으면 생성, 있으면 그대로 반환 (멱등)
    @GetMapping("/chatbot/channel")
    public ChatbotChannelResponse getChannel(@AuthenticationPrincipal CustomUserPrincipal principal) {
        String channelUrl = chatbotChannelQueryUseCase.getChannelUrl(principal.getId());
        return new ChatbotChannelResponse(channelUrl);
    }

}

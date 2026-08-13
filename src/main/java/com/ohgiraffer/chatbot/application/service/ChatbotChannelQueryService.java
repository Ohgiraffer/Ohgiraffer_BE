package com.ohgiraffer.chatbot.application.service;

import com.ohgiraffer.chatbot.application.helper.ChatbotChannelLockedProvisioner;
import com.ohgiraffer.chatbot.application.port.ChatbotChannelPort;
import com.ohgiraffer.chatbot.application.usecase.ChatbotChannelQueryUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/*
 * comment.
 *  ChatbotChannelQueryUseCase 구현체
 *  - 캐시 히트 시 즉시 반환(락 없이 빠른 경로)
 *  - 캐시 미스 시에만 ChatbotChannelLockedProvisioner로 위임해 분산락 하에서 생성
 *  - BriefingQueryService와 동일 패턴
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotChannelQueryService implements ChatbotChannelQueryUseCase {

    private final ChatbotChannelPort chatbotChannelPort;
    private final ChatbotChannelLockedProvisioner chatbotChannelLockedProvisioner;

    // 캐시 조회 우선, 없으면 분산락 하에서 생성 위임
    @Override
    public String getChannelUrl(Long userId) {
        return chatbotChannelPort.findChannelUrl(userId)
                .orElseGet(() -> chatbotChannelLockedProvisioner.provision(userId));
    }

}

package com.ohgiraffer.chatbot.application.helper;

import com.ohgiraffer.chatbot.application.port.ChatbotChannelPort;
import com.ohgiraffer.global.aop.lock.DistributedLock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/*
 * comment.
 *  ChatbotChannelProvisioner를 분산락으로 감싼 래퍼
 *  - BriefingLockedGenerator와 동일 패턴 - 동시 요청으로 채널이 중복 생성되는 것 방지
 */

@Component
@RequiredArgsConstructor
public class ChatbotChannelLockedProvisioner {

    private final ChatbotChannelPort chatbotChannelPort;
    private final ChatbotChannelProvisioner chatbotChannelProvisioner;

    // 락 획득 후 재확인 - 대기 중 다른 스레드가 이미 만들었을 수 있음
    @DistributedLock(
            key = "'ai:chatbot:channel:lock:' + #userId",
            waitTime = 10,
            leaseTime = 20,
            timeUnit = TimeUnit.SECONDS
    )
    public String provision(Long userId) {
        return chatbotChannelPort.findChannelUrl(userId)
                .orElseGet(() -> chatbotChannelProvisioner.createAndSave(userId));
    }

}

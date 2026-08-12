package com.ohgiraffer.chatbot.application.helper;

import com.ohgiraffer.chatbot.application.port.ChatbotSessionPort;
import com.ohgiraffer.chatbot.domain.model.ChatbotSessionTurn;
import com.ohgiraffer.global.aop.lock.DistributedLock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/* comment.
 *  챗봇 세션 read-modify-write 구간에 분산락 적용
 *  - BriefingLockedGenerator와 동일 패턴(@DistributedLock) 재사용
 *  - waitTime/leaseTime은 브리핑(50/100초)보다 짧게 - 세션 갱신은 훨씬 빈번하고 가벼운 작업이라 조정함
 */

@Component
@RequiredArgsConstructor
public class ChatbotSessionLockedUpdater {

    private final ChatbotSessionPort chatbotSessionPort;

    @DistributedLock(
            key = "'ai:chatbot:session:lock:' + #userId",
            waitTime = 10,
            leaseTime = 20,
            timeUnit = TimeUnit.SECONDS
    )
    public List<ChatbotSessionTurn> updateHistory(Long userId, Function<List<ChatbotSessionTurn>, List<ChatbotSessionTurn>> mutator) {
        List<ChatbotSessionTurn> current = chatbotSessionPort.findHistory(userId);
        List<ChatbotSessionTurn> updated = mutator.apply(current);
        chatbotSessionPort.appendAndSave(userId, updated);
        return updated;
    }

}

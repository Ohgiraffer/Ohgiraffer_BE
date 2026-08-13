package com.ohgiraffer.chatbot.application.helper;

import com.ohgiraffer.chatbot.application.port.ChatbotSessionPort;
import com.ohgiraffer.chatbot.domain.model.ChatbotSessionTurn;
import com.ohgiraffer.global.aop.lock.DistributedLock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/*
 * comment.
 *  챗봇 세션 read-modify-write 구간에 분산락 적용
 *  - BriefingLockedGenerator와 동일 패턴(@DistributedLock) 재사용
 *  - leaseTime은 ChatbotOrchestrator의 MAX_HOPS(5) x Gemini 호출 최대 소요시간을
 *    여유 있게 덮어야 함 - 그렇지 않으면 처리 도중 락이 풀려 동시 갱신에 의한 히스토리 유실 위험이 있음
 */

@Component
@RequiredArgsConstructor
public class ChatbotSessionLockedUpdater {

    private final ChatbotSessionPort chatbotSessionPort;

    @DistributedLock(
            key = "'ai:chatbot:session:lock:' + #userId",
            waitTime = 10,
            leaseTime = 480,
            timeUnit = TimeUnit.SECONDS
    )
    public List<ChatbotSessionTurn> updateHistory(Long userId, Function<List<ChatbotSessionTurn>, List<ChatbotSessionTurn>> mutator) {
        List<ChatbotSessionTurn> current = chatbotSessionPort.findHistory(userId);
        List<ChatbotSessionTurn> updated = mutator.apply(current);
        chatbotSessionPort.appendAndSave(userId, updated);
        return updated;
    }

}

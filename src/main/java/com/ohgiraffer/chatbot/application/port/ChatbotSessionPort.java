package com.ohgiraffer.chatbot.application.port;

import com.ohgiraffer.chatbot.domain.model.ChatbotSessionTurn;

import java.util.List;

/* comment.
 *  챗봇 세션(대화 히스토리) 캐시 포트
 *  - RedisBriefingCacheAdapter와 유사한 성격이나, 단일 값이 아닌 히스토리 배열을 다룸
 */

public interface ChatbotSessionPort {

    // 현재까지의 대화 히스토리 조회 (없으면 빈 리스트)
    List<ChatbotSessionTurn> findHistory(Long userId);

    // 대화 히스토리 갱신 - 최근 N턴 초과분은 구현체 내부에서 잘라냄, TTL도 매번 갱신(sliding)
    void appendAndSave(Long userId, List<ChatbotSessionTurn> updatedHistory);

    // 세션 초기화 (TTL 만료 외에 명시적 초기화가 필요한 경우용)
    void clear(Long userId);

}

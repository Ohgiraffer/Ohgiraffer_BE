package com.ohgiraffer.chatbot.infrastructure.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.ohgiraffer.chatbot.application.port.ChatbotSessionPort;
import com.ohgiraffer.chatbot.domain.model.ChatbotSessionTurn;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/*
 * comment.
 *  ChatbotSessionPort 실구현체
 *  - 키 패턴: ai:chatbot:session:{userId} (ai:briefing:{userId} 네임스페이스 컨벤션과 동일선상)
 *  - TTL: 30분 sliding (매 저장마다 갱신) - 조회 조회만으로는 TTL 갱신 안 함(대화 진행 시에만 연장)
 *  - 히스토리 최근 MAX_TURNS(20 = user/model 등 10턴 쌍)만 유지, 초과분은 오래된 것부터 제거
 *  - ObjectMapper는 Spring Boot 4 Jackson 3 빈 충돌 회피를 위해 직접 생성(SendbirdApiAdapter 컨벤션과 동일)
 */

@Slf4j
@Component
public class RedisChatbotSessionAdapter implements ChatbotSessionPort {

    private static final String KEY_PREFIX = "ai:chatbot:session:";
    private static final Duration SESSION_TTL = Duration.ofMinutes(30); // sliding, 튜닝 대상
    private static final int MAX_TURNS = 20; // 초기값, 실사용 후 튜닝 대상

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RedisChatbotSessionAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }

    @Override
    public List<ChatbotSessionTurn> findHistory(Long userId) {
        String raw = redisTemplate.opsForValue().get(key(userId));
        if (raw == null || raw.isBlank()) {
            return Collections.emptyList();
        }
        try {
            CollectionType listType = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, ChatbotSessionTurn.class);
            return objectMapper.readValue(raw, listType);
        } catch (Exception e) {
            // 역직렬화 실패 시 세션을 못 믿는 상태 - 새 대화로 취급 (전체 실패보다 안전한 쪽 선택)
            log.warn("[ChatbotSession] 히스토리 역직렬화 실패, 세션 초기화 취급 | userId={}", userId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public void appendAndSave(Long userId, List<ChatbotSessionTurn> updatedHistory) {
        List<ChatbotSessionTurn> trimmed = trimToMaxTurns(updatedHistory);
        try {
            String json = objectMapper.writeValueAsString(trimmed);
            redisTemplate.opsForValue().set(key(userId), json, SESSION_TTL); // set 시점에 TTL 재설정 = sliding
        } catch (Exception e) {
            log.error("[ChatbotSession] 히스토리 직렬화/저장 실패 | userId={}", userId, e);
        }
    }

    @Override
    public void clear(Long userId) {
        redisTemplate.delete(key(userId));
    }

    // 최근 MAX_TURNS개 초과분은 오래된 것부터 제거 (토큰 비용 관리)
    private List<ChatbotSessionTurn> trimToMaxTurns(List<ChatbotSessionTurn> history) {
        if (history.size() <= MAX_TURNS) {
            return history;
        }
        return history.subList(history.size() - MAX_TURNS, history.size());
    }

    private String key(Long userId) {
        return KEY_PREFIX + userId;
    }

}

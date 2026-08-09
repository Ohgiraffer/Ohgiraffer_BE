package com.ohgiraffer.aiassistant.infrastructure.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ohgiraffer.aiassistant.application.port.BriefingCachePort;
import com.ohgiraffer.aiassistant.domain.model.BriefingSummary;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/* comment.
 *  BriefingCachePort 실구현체
 *  - 기존 로그인 관련 RedisTemplate<String,String> 그대로 재사용, BriefingSummary는 JSON 문자열로 직렬화해서 저장
 *  - key 패턴: ai:briefing:{userId}, TTL 24시간
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisBriefingCacheAdapter implements BriefingCachePort {

    private static final String KEY_PREFIX = "ai:briefing:";
    private static final Duration TTL = Duration.ofHours(24);

    private final RedisTemplate<String, String> redisTemplate;  // 기존 빈 재사용
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());  // LocalDateTime 직렬화 위해 직접 생성 (SendbirdApiAdapter 패턴과 동일)

    @Override
    public Optional<BriefingSummary> find(Long userId) {
        String raw = redisTemplate.opsForValue().get(KEY_PREFIX + userId);
        if (raw == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(raw, BriefingSummary.class));
        } catch (Exception e) {
            log.warn("[Briefing] 캐시 역직렬화 실패, 캐시 없는 것으로 처리 | userId={}", userId, e);
            return Optional.empty();
        }
    }

    @Override
    public void save(BriefingSummary summary) {
        try {
            String json = objectMapper.writeValueAsString(summary);
            redisTemplate.opsForValue().set(KEY_PREFIX + summary.userId(), json, TTL);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AI_API_CALL_FAILED, "브리핑 캐시 저장 실패");
        }
    }

}

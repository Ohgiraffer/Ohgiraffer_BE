package com.ohgiraffer.aiassistant.application.service;

import com.ohgiraffer.aiassistant.application.helper.BriefingGenerator;
import com.ohgiraffer.aiassistant.application.port.BriefingCachePort;
import com.ohgiraffer.aiassistant.application.usecae.BriefingQueryUseCase;
import com.ohgiraffer.aiassistant.domain.model.BriefingSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/* comment.
 *  BriefingQueryUseCase 구현체
 *  - 캐시 히트 시 즉시 반환
 *  - 캐시 미스 시 userId 기준 분산락(Redis SETNX)으로 감싸서 동시 요청 시 Gemini 중복 호출 방지
 *  - 락 획득 실패 시 짧게 재시도하며 캐시 재확인, 그래도 안 채워지면 락 없이 직접 생성(데드락/무한대기 방지)
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class BriefingQueryService implements BriefingQueryUseCase {

    private static final String LOCK_KEY_PREFIX = "ai:briefing:lock:";
    private static final Duration LOCK_TTL = Duration.ofSeconds(10);
    private static final int MAX_RETRY = 5;
    private static final long RETRY_INTERVAL_MS = 200;

    private final BriefingCachePort briefingCachePort;
    private final BriefingGenerator briefingGenerator;
    private final RedisTemplate<String, String> redisTemplate;  // 기존 빈 재사용, 락 전용 키만 별도 prefix

    @Override
    public BriefingSummary getBriefing(Long userId) {
        Optional<BriefingSummary> cached = briefingCachePort.find(userId);
        if (cached.isPresent()) {
            return cached.get();
        }
        return getBriefingWithLock(userId);
    }

    // 캐시 미스 시에만 진입 - 락 획득 후 캐시 재확인(더블 체크)하고 없으면 생성
    private BriefingSummary getBriefingWithLock(Long userId) {
        String lockKey = LOCK_KEY_PREFIX + userId;
        boolean acquired = Boolean.TRUE.equals(
                redisTemplate.opsForValue().setIfAbsent(lockKey, "1", LOCK_TTL)
        );

        if (!acquired) {
            // 다른 요청이 이미 생성 중 - 짧게 대기하며 캐시 재확인
            for (int attempt = 0; attempt < MAX_RETRY; attempt++) {
                sleep(RETRY_INTERVAL_MS);
                Optional<BriefingSummary> retryCached = briefingCachePort.find(userId);
                if (retryCached.isPresent()) {
                    return retryCached.get();
                }
            }
            // 대기 끝까지 캐시 안 채워짐 - 락 없이 직접 생성 (무한 대기 방지)
            log.warn("[Briefing] 락 대기 타임아웃, 락 없이 직접 생성 | userId={}", userId);
            return briefingGenerator.generateAndCache(userId);
        }

        try {
            // 더블 체크 - 락 대기 사이 다른 스레드가 이미 채웠을 수 있음
            return briefingCachePort.find(userId)
                    .orElseGet(() -> briefingGenerator.generateAndCache(userId));
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}

package com.ohgiraffer.aiassistant.application.service;

import com.ohgiraffer.aiassistant.application.helper.BriefingGenerator;
import com.ohgiraffer.aiassistant.application.port.BriefingCachePort;
import com.ohgiraffer.aiassistant.application.usecae.BriefingQueryUseCase;
import com.ohgiraffer.aiassistant.domain.model.BriefingSummary;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

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
    private static final Duration LOCK_TTL = Duration.ofSeconds(100);  // Gemini 기본 타임아웃(90s) + 여유분
    private static final Duration MAX_WAIT = LOCK_TTL; // 재시도 전체 대기시간 = 락 TTL과 동일하게 맞춤
    private static final long RETRY_INTERVAL_MS = 1000; // 1초 간격으로 캐시/락 재확인

    // 토큰이 일치할 때만 원자적으로 삭제 - GET+DELETE를 별도 호출하면 그 사이 TTL 만료로 다른 요청 락을 지울 수 있어 Lua로 원자화
    private static final RedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "return redis.call('del', KEYS[1]) " +
                    "else return 0 end",
            Long.class
    );

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
        String lockToken = UUID.randomUUID().toString();  // 이 요청만의 고유 토큰

        boolean acquired = Boolean.TRUE.equals(
                redisTemplate.opsForValue().setIfAbsent(lockKey, lockToken, LOCK_TTL)
        );

        if (acquired) {
            return generateWithLockHeld(userId, lockKey, lockToken);
        }

        return waitAndRetryAcquire(userId, lockKey);

    }

    // 락을 이미 쥔 상태에서 더블 체크 후 생성, 끝나면 반드시 본인 락만 해제
    private BriefingSummary generateWithLockHeld(Long userId, String lockKey, String lockToken) {
        try {
            return briefingCachePort.find(userId)
                    .orElseGet(() -> briefingGenerator.generateAndCache(userId));
        } finally {
            releaseLockIfOwned(lockKey, lockToken);
        }
    }

    // 락 획득 실패 시: MAX_WAIT(=LOCK_TTL)만큼 대기하며 매 주기 (1)캐시 확인 (2)락 재획득 시도
    // 원 소유자가 끝까지 못 채우고 락을 반납하면, 대기 중인 요청이 락을 넘겨받아 직접 생성함
    private BriefingSummary waitAndRetryAcquire(Long userId, String lockKey) {
        Instant deadline = Instant.now().plus(MAX_WAIT);

        while (Instant.now().isBefore(deadline)) {
            sleep(RETRY_INTERVAL_MS);

            Optional<BriefingSummary> retryCached = briefingCachePort.find(userId);
            if (retryCached.isPresent()) {
                return retryCached.get();
            }

            // 캐시가 아직 없으면 락이 비어있는지(원 소유자가 실패해서 반납했는지) 확인차 재획득 시도
            String retryToken = UUID.randomUUID().toString();
            boolean reacquired = Boolean.TRUE.equals(
                    redisTemplate.opsForValue().setIfAbsent(lockKey, retryToken, LOCK_TTL)
            );
            if (reacquired) {
                log.info("[Briefing] 원 소유자 락 반납 감지, 락 재획득 후 직접 생성 | userId={}", userId);
                return generateWithLockHeld(userId, lockKey, retryToken);
            }
        }

        log.warn("[Briefing] 최대 대기시간({}s) 초과, 브리핑 생성 실패로 처리 | userId={}", MAX_WAIT.getSeconds(), userId);
        throw new BusinessException(ErrorCode.AI_API_CALL_FAILED, "브리핑 생성이 지연되고 있습니다. 잠시 후 다시 시도해주세요.");
    }

    // 본인이 잡은 락(토큰 일치)일 때만 원자적으로 삭제 - 남의 락을 실수로 지우지 않도록 보장
    private void releaseLockIfOwned(String lockKey, String lockToken) {
        redisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(lockKey), lockToken);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
